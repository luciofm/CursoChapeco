package curso.despesas.DAO;

import org.kroz.activerecord.ActiveRecordBase;

public class Usuario extends ActiveRecordBase {
	public int user_id;
	public String nome;

	public Usuario() {
		
	}

	public Usuario(int id, String nome) {
		super();
		this.user_id = id;
		this.nome = nome;
	}

	public int getId() {
		return user_id;
	}

	public void setId(int id) {
		this.user_id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public String toString() {
		return "Usuario [id=" + user_id + ", nome=" + nome + "]";
	}
}


