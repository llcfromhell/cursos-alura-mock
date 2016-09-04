package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.junit.Test;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.facade.RepositorioDeLeiloes;
import br.com.caelum.leilao.infra.dao.LeilaoDao; 

public class EncerradorDeLeilaoTest {
	
	@Test
	public void testaQueEncerrouLeilao() {
		
		Calendar dataEncerrada = Calendar.getInstance();
		dataEncerrada.set(2016,01,01);
		
		LeilaoDao daoFalso = mock(LeilaoDao.class);
		
		Leilao leilao1  = new CriadorDeLeilao().para("razer cynosa pro").naData(dataEncerrada).constroi();
		Leilao leilao2  = new CriadorDeLeilao().para("gtx970").naData(dataEncerrada).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
        encerrador.encerra();
        
        assertEquals(2, encerrador.getTotalEncerrados());
        
        assertTrue(leilao1.isEncerrado());
        assertTrue(leilao2.isEncerrado());
		
	}
	
	@Test
	public void testaQueLeilaoAindaEhAtivo() {
		
		Calendar dataEncerrada = Calendar.getInstance();
		dataEncerrada.add(Calendar.DAY_OF_MONTH, -1);
		
		RepositorioDeLeiloes daoFalso = mock(RepositorioDeLeiloes.class);
		
		Leilao leilao1  = new CriadorDeLeilao().para("razer cynosa pro").naData(dataEncerrada).constroi();
		Leilao leilao2  = new CriadorDeLeilao().para("gtx970").naData(dataEncerrada).constroi();
		
		List<Leilao> leiloesAntigos = Arrays.asList(leilao1, leilao2);
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
        encerrador.encerra();
        
        assertEquals(0, encerrador.getTotalEncerrados());
        
        assertFalse(leilao1.isEncerrado());
        assertFalse(leilao2.isEncerrado());
		
	}
	
	@Test
	public void testaQueNaoFazNadaQuandoVazio() {
		
		LeilaoDao daoFalso = mock(LeilaoDao.class);
		
		List<Leilao> leiloesAntigos = new ArrayList<Leilao>();
		
		when(daoFalso.correntes()).thenReturn(leiloesAntigos);
		
		EncerradorDeLeilao encerrador = new EncerradorDeLeilao(daoFalso);
        encerrador.encerra();
        
        assertEquals(0, encerrador.getTotalEncerrados());
		
	}
	

}
