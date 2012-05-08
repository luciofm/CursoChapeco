package curso.despesas.DAO;

import org.kroz.activerecord.ActiveRecordBase;

public class Usuario extends ActiveRecordBase {
	public int id;
	public String nome;

	public Usuario() {
		
	}

	public Usuario(int id, String nome) {
		super();
		this.id = id;
		this.nome = nome;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public String toString() {
		return "Usuario [id=" + id + ", nome=" + nome + "]";
	}
}


