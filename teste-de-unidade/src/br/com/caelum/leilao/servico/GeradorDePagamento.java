package br.com.caelum.leilao.servico;

import java.util.Calendar;
import java.util.List;

import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Pagamento;
import br.com.caelum.leilao.facade.RepositorioDeLeiloes;
import br.com.caelum.leilao.facade.RepositorioDePagamentos;
import br.com.caelum.leilao.infra.Relogio;
import br.com.caelum.leilao.infra.RelogioDoSistema;

public class GeradorDePagamento {

	private final RepositorioDePagamentos pagamentos;
	private final RepositorioDeLeiloes leiloes;
	private final Avaliador avaliador;
	private final Relogio relogio;

	public GeradorDePagamento(RepositorioDeLeiloes leiloes, RepositorioDePagamentos pagamentos, Avaliador avaliador) {
		this(leiloes, pagamentos, avaliador, new RelogioDoSistema());
	}

	public GeradorDePagamento(RepositorioDeLeiloes leiloes, RepositorioDePagamentos pagamentos, Avaliador avaliador,
			Relogio relogio) {
		this.leiloes = leiloes;
		this.pagamentos = pagamentos;
		this.avaliador = avaliador;
		this.relogio = relogio;
	}

	public void gera() {

		List<Leilao> leiloesEncerrados = leiloes.encerrados();
		for (Leilao leilao : leiloesEncerrados) {
			avaliador.avalia(leilao);

			Pagamento novoPagamento = new Pagamento(avaliador.getMaiorLance(), primeiroDiaUtil());
			pagamentos.salvar(novoPagamento);
		}

	}

	private Calendar primeiroDiaUtil() {

		int diaDaSemana = relogio.hoje().get(Calendar.DAY_OF_WEEK);

		if (diaDaSemana == Calendar.SATURDAY)
			relogio.hoje().add(Calendar.DAY_OF_MONTH, 2);
		else if (diaDaSemana == Calendar.SUNDAY)
			relogio.hoje().add(Calendar.DAY_OF_MONTH, 1);

		return relogio.hoje();
	}

}
