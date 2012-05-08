package curso.despesas.DAO;

import org.kroz.activerecord.ActiveRecordBase;

public class Despesas extends ActiveRecordBase {
	public int user_id;
	public double valor;
	public String descricao;

	public Despesas() {
	}

	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public double getValor() {
		return valor;
	}

	public void setValor(double valor) {
		this.valor = valor;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	@Override
	public String toString() {
		return "Despesas [user_id=" + user_id + ", valor=" + valor
				+ ", descricao=" + descricao + "]";
	}
}
