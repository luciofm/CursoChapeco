package curso.despesas;

import org.kroz.activerecord.ActiveRecordBase;
import org.kroz.activerecord.ActiveRecordException;
import org.kroz.activerecord.Database;
import org.kroz.activerecord.DatabaseBuilder;

import android.app.Application;
import android.util.Log;
import curso.despesas.DAO.Despesas;
import curso.despesas.DAO.Usuario;

public class DespesasApp extends Application {

	public ActiveRecordBase mDatabase;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Despesas", "Iniciando APP despesas");

		DatabaseBuilder builder = new DatabaseBuilder("despesas.db");
		builder.addClass(Usuario.class);
		builder.addClass(Despesas.class);
		Database.setBuilder(builder);
		
		try {
			mDatabase = ActiveRecordBase.open(this, "despesas.db", 3);
		} catch (ActiveRecordException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	public ActiveRecordBase getDatabase() {
		return mDatabase;
	}
}
