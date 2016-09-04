package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.facade.EnviadorDeEmail;
import br.com.caelum.leilao.infra.dao.LeilaoDao; 

public class EncerradorDeLeilaoTest {
	
	private LeilaoDao daoFalso;
	private EnviadorDeEmail carteiroFalso;
	private EncerradorDeLeilao encerrador;

	@Before
	public void before() {
		daoFalso = mock(LeilaoDao.class);
		carteiroFalso = mock(EnviadorDeEmail.class);
		encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
	}
	
	@Test
	public void testaQueEncerrouLeilao() {
		
		Calendar dataEncerrada = Calendar.getInstance();
		dataEncerrada.set(2016,01,01);
		
		Leilao leilao1  = new CriadorDeLeilao().para("razer cynosa pro").naData(dataEncerrada).constroi();
		Leilao leilao2  = new CriadorDeLeilao().para("gtx970").naData(dataEncerrada).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
        encerrador.encerra();
        
        assertEquals(2, encerrador.getTotalEncerrados());
        
        assertTrue(leilao1.isEncerrado());
        assertTrue(leilao2.isEncerrado());
		
	}
	
	@Test
	public void testaQueLeilaoAindaEhAtivo() {
		
		Calendar dataEncerrada = Calendar.getInstance();
		dataEncerrada.add(Calendar.DAY_OF_MONTH, -1);
		
		Leilao leilao1  = new CriadorDeLeilao().para("razer cynosa pro").naData(dataEncerrada).constroi();
		Leilao leilao2  = new CriadorDeLeilao().para("gtx970").naData(dataEncerrada).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
        encerrador.encerra();
        
        assertEquals(0, encerrador.getTotalEncerrados());
        
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());
        
        verify(daoFalso, never()).atualiza(leilao1);
        verify(daoFalso, never()).atualiza(leilao2);
		
	}
	
	@Test
    public void deveAtualizarLeiloesEncerrados() {

        Calendar antiga = Calendar.getInstance();
        antiga.set(1999, 1, 20);

        Leilao leilao1 = new CriadorDeLeilao().para("TV de plasma")
            .naData(antiga).constroi();

        when(daoFalso.correntes()).thenReturn(Arrays.asList(leilao1));

        encerrador.encerra();

        // verificando que o metodo atualiza foi realmente invocado!
        verify(daoFalso, times(1)).atualiza(leilao1);
        
        // passamos os mocks que serao verificados
        InOrder inOrder = inOrder(daoFalso, carteiroFalso);
        // a primeira invocação
        inOrder.verify(daoFalso, times(1)).atualiza(leilao1);    
        // a segunda invocação
        inOrder.verify(carteiroFalso, times(1)).envia(leilao1);
    }
	
	@Test
	public void testaQueNaoFazNadaQuandoVazio() {
		
		List<Leilao> leiloesAntigos = new ArrayList<Leilao>();
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
        encerrador.encerra();
        
        assertEquals(0, encerrador.getTotalEncerrados());
		
	}
	

}
