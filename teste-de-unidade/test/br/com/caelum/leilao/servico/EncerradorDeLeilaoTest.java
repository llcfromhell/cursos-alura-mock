package br.com.caelum.leilao.servico;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.infra.dao.LeilaoDao; 

public class EncerradorDeLeilaoTest {
	
	private LeilaoDao daoFalso;
	private EnviadorDeEmail carteiroFalso;
	private EncerradorDeLeilao encerrador;
	private Calendar dataEncerrada;

	@Before
	public void before() {
		daoFalso = mock(LeilaoDao.class);
		carteiroFalso = mock(EnviadorDeEmail.class);
		encerrador = new EncerradorDeLeilao(daoFalso, carteiroFalso);
		dataEncerrada = Calendar.getInstance();
		dataEncerrada.set(2016,01,01);
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
	
	@Test
	public void testaNaoParaQuandoGeraErro() {
		
		Leilao leilao1 = new CriadorDeLeilao().para("blink 182 album").naData(dataEncerrada).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("the offspring album").naData(dataEncerrada).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(leilao1);
		
        encerrador.encerra();
        
        assertEquals(1, encerrador.getTotalEncerrados());
        
        // tem fazer o processo pro leilao 2 mesmo que de erro no anterior
        verify(daoFalso).atualiza(leilao2);
        verify(carteiroFalso).envia(leilao2);
		
	}
	
	@Test
	public void testaErroAoEnviarEmailNaoInterrompe() {
		
		Leilao leilao1 = new CriadorDeLeilao().para("guitarra fender").naData(dataEncerrada).constroi();
		Leilao leilao2 = new CriadorDeLeilao().para("bateria mapex").naData(dataEncerrada).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		doThrow(new RuntimeException()).when(carteiroFalso).envia(leilao1);
		
        encerrador.encerra();
        
        assertEquals(1, encerrador.getTotalEncerrados());
        
        // tem fazer o processo pro leilao 2 mesmo que de erro no anterior
        verify(daoFalso).atualiza(leilao2);
        verify(carteiroFalso).envia(leilao2);
		
	}
	
	@Test
	public void testaErroEmTodosOsAcessoAoDadoNaoEnviaEmail() {
		
		List<Leilao> leiloesAntigos = Arrays.asList(
				new CriadorDeLeilao().para("guitarra fender").naData(dataEncerrada).constroi(),
				new CriadorDeLeilao().para("bateria mapex").naData(dataEncerrada).constroi(),
				new CriadorDeLeilao().para("contrabaixo fender").naData(dataEncerrada).constroi());
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		doThrow(new RuntimeException()).when(daoFalso).atualiza(any(Leilao.class));
		
        encerrador.encerra();
        
        assertEquals(0, encerrador.getTotalEncerrados());

        verify(carteiroFalso, never()).envia(any(Leilao.class));
		
	}


}
