package br.com.caelum.leilao.servico;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import br.com.caelum.leilao.builder.CriadorDeLeilao;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.dominio.Usuario;
import br.com.caelum.leilao.facade.RepositorioDeLeiloes;
import br.com.caelum.leilao.facade.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.Relogio;

public class GeradorDePagamentoTest {

	private Calendar domingo;
	private Calendar sabado;

	@Before
	public void before() {
		domingo = Calendar.getInstance();
		domingo.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		
		sabado = Calendar.getInstance();
		sabado.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
	}
	
	@Test
	public void deveGerarPagamentoParaUmLeilaoEncerrado() {

		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Avaliador avaliador = new Avaliador();

		Leilao leilao = new CriadorDeLeilao().para("Playstation").lance(new Usuario("José da Silva"), 2000.0)
				.lance(new Usuario("Maria Pereira"), 2500.0).constroi();

		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, avaliador);
		gerador.gera();

		assertEquals(avaliador.getMaiorLance(), 2500.0, 0.00);

		// como fazer assert no Pagamento gerado?

		// criamos o ArgumentCaptor que sabe capturar um Pagamento
		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		// capturamos o Pagamento que foi passado para o método salvar
		verify(pagamentos).salvar(argumento.capture());

		Pagamento pagamentoGerado = argumento.getValue();
		assertEquals(2500.0, pagamentoGerado.getValor(), 0.00001);
	}

	@Test
	public void deveEmpurrarParaOProximoDiaUtilQuandoForSabado() {

		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Relogio relogioSabado = mock(Relogio.class);
		
		when(relogioSabado.hoje()).thenReturn(sabado);

		Leilao leilao = new CriadorDeLeilao().para("Playstation").lance(new Usuario("José da Silva"), 2000.0)
				.lance(new Usuario("Maria Pereira"), 2500.0).constroi();

		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogioSabado);
		gerador.gera();

		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salvar(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();

		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
	}
	
	@Test
	public void deveEmpurrarParaOProximoDiaUtilQuandoForDomingo() {

		RepositorioDeLeiloes leiloes = mock(RepositorioDeLeiloes.class);
		RepositorioDePagamentos pagamentos = mock(RepositorioDePagamentos.class);
		Relogio relogioDomingo = mock(Relogio.class);
		
		when(relogioDomingo.hoje()).thenReturn(domingo);

		Leilao leilao = new CriadorDeLeilao().para("Playstation").lance(new Usuario("José da Silva"), 2000.0)
				.lance(new Usuario("Maria Pereira"), 2500.0).constroi();

		when(leiloes.encerrados()).thenReturn(Arrays.asList(leilao));

		GeradorDePagamento gerador = new GeradorDePagamento(leiloes, pagamentos, new Avaliador(), relogioDomingo);
		gerador.gera();

		ArgumentCaptor<Pagamento> argumento = ArgumentCaptor.forClass(Pagamento.class);
		verify(pagamentos).salvar(argumento.capture());
		Pagamento pagamentoGerado = argumento.getValue();

		assertEquals(Calendar.MONDAY, pagamentoGerado.getData().get(Calendar.DAY_OF_WEEK));
	}

}
