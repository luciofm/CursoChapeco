package curso.despesas;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.kroz.activerecord.ActiveRecordBase;
import org.kroz.activerecord.ActiveRecordException;
import org.kroz.activerecord.EntitiesHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import curso.despesas.DAO.Usuario;
import curso.despesas.DTO.UsuarioDTO;
import curso.utils.Networking;

public class ListaUsuariosActivity extends ListActivity {

	private static final int DIALOG_ADD_USER = 0;
	private static final int DIALOG_ABOUT = 1;

	ActiveRecordBase db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.usuarios);

		db = ((DespesasApp) getApplication()).getDatabase();

		new ListaUsuariosTask(this).execute(getString(R.string.usuarios_url));

		getListView().setOnItemLongClickListener(longClick);
		
		AdView adView = (AdView) findViewById(R.id.ads);
		adView.loadAd(new AdRequest());
	}

	OnItemLongClickListener longClick = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View view,
				int pos, long id) {
			final Usuario usuario = (Usuario) adapter.getItemAtPosition(pos);
			AlertDialog.Builder builder = new AlertDialog.Builder(
					ListaUsuariosActivity.this);
			builder.setMessage("Você deseja remover o usuario "
					+ usuario.getNome())
					.setCancelable(true)
					.setNegativeButton("Não", new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					UsuariosAdapter adapter = (UsuariosAdapter) getListAdapter();
					adapter.remove(usuario);
					adapter.notifyDataSetChanged();
					try {
						boolean resposta = Networking
							.deleteHttpRequest(getString(R.string.base_url)
										+ usuario.getId() + ".json");
						if (resposta)
							usuario.delete();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.add_usuario, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.add) {
			showDialog(DIALOG_ADD_USER);
			return true;
		}
		if (item.getItemId() == R.id.refresh) {
			setListAdapter(null);
			new ListaUsuariosTask(this).execute(getString(R.string.usuarios_url));
			return true;
		}
		if (item.getItemId() == R.id.about) {
			showDialog(DIALOG_ABOUT);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ADD_USER:
			return createAddUserDialog();
		case DIALOG_ABOUT:
			return createAboutDialog();
		}
		return super.onCreateDialog(id);
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id == DIALOG_ADD_USER) {
			EditText text = (EditText) dialog.findViewById(R.id.editUsername);
			text.setText("");
		}
		super.onPrepareDialog(id, dialog);
	}

	private Dialog createAboutDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Despesas 1.0").setCancelable(true)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		return builder.create();
	}

	private Dialog createAddUserDialog() {
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.add_usuario);
		dialog.setTitle("Adiciona usuário");
		Button okButton = (Button) dialog.findViewById(R.id.buttonAdd);
		Button cancelButton = (Button) dialog.findViewById(R.id.buttonCancel);
		final EditText text = (EditText) dialog.findViewById(R.id.editUsername);

		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String username = text.getText().toString();
				adicionarUsuario(username);
				dialog.cancel();
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}
		});

		return dialog;
	}

	protected void adicionarUsuario(String username) {
		new CriaUsuario(db, this).execute(getString(R.string.usuarios_url),
				username);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Usuario usuario = (Usuario) l.getItemAtPosition(position);
		Intent intent = new Intent(this, ListaDespesasActivity.class);
		intent.putExtra("ID", usuario.getId());
		intent.putExtra("NOME", usuario.getNome());
		startActivity(intent);
	}

	public class ListaUsuariosTask extends AsyncTask<String, String, ArrayList<Usuario>> {

		ListaUsuariosActivity activity;

		public ListaUsuariosTask(Context context) {
			activity = (ListaUsuariosActivity) context;
		}

		@Override
		protected void onPostExecute(ArrayList<Usuario> result) {
			if (result != null)
				activity.setListAdapter(new UsuariosAdapter(activity,
						0, result));
		}

		@Override
		protected ArrayList<Usuario> doInBackground(String... params) {
			String url = params[0];
			String resposta;
			try {
				resposta = Networking.getHttpRequest(url);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				try {
					return (ArrayList<Usuario>) db.find(Usuario.class, null,
							null);
				} catch (ActiveRecordException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return null;
				}
			}

			try {
				db.delete(Usuario.class, null, null);
			} catch (ActiveRecordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Type type = new TypeToken<ArrayList<UsuarioJson>>() {
			}.getType();
			Gson gson = new GsonBuilder().create();
			ArrayList<UsuarioJson> temp = gson.fromJson(resposta, type);
			for (UsuarioJson u : temp) {
				Log.d("Despesas", "Usuario: " + u.getUsuario().toString());
				try {
					Usuario usuario = db.newEntity(Usuario.class);
					EntitiesHelper.copyFieldsWithoutID(usuario, u.getUsuario());
					usuario.setId(u.getUsuario().getId());
					usuario.save();
				} catch (ActiveRecordException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			try {
				return (ArrayList<Usuario>) db.findAll(Usuario.class);
			} catch (ActiveRecordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	};

	public class CriaUsuario extends AsyncTask<String, Void, Usuario> {

		ActiveRecordBase db;
		Context context;
		ProgressDialog dialog;

		public CriaUsuario(ActiveRecordBase db, Context context) {
			this.db = db;
			this.context = context;
		}

		@Override
		protected void onPostExecute(Usuario result) {
			dialog.dismiss();
			if (result == null) {
				Toast.makeText(context, "Erro adicionando usuario",
						Toast.LENGTH_LONG).show();
			} else {
				UsuariosAdapter adapter = (UsuariosAdapter) 
						((ListaUsuariosActivity) context)
						.getListAdapter();
				adapter.add(result);
				adapter.notifyDataSetChanged();
			}
		}

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(context, "Despesas",
					"Adicionando usuário");
		}

		@Override
		protected Usuario doInBackground(String... params) {
			String url = params[0];
			String username = params[1];
			UsuarioJson user = new UsuarioJson();
			user.usuario = new UsuarioDTO();
			user.usuario.setNome(username);
			Gson gson = new GsonBuilder().create();
			String content = gson.toJson(user, UsuarioJson.class);

			String resposta;
			try {
				resposta = Networking.postHttpRequest(url, content);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

			UsuarioJson u = gson.fromJson(resposta, UsuarioJson.class);
			try {
				Usuario usuario = db.newEntity(Usuario.class);
				EntitiesHelper.copyFieldsWithoutID(usuario, u.getUsuario());
				usuario.setId(u.getUsuario().getId());
				usuario.save();
				return usuario;
			} catch (ActiveRecordException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

	}

	public class UsuariosAdapter extends ArrayAdapter<Usuario> {
		LayoutInflater inflater;

		public UsuariosAdapter(Context context, int textViewResourceId,
				List<Usuario> objects) {
			super(context, textViewResourceId, objects);
			inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null)
				v = inflater.inflate(android.R.layout.simple_list_item_1,
						parent, false);
			TextView text = (TextView) v.findViewById(android.R.id.text1);
			Usuario u = getItem(position);
			text.setText(u.getNome());
			return v;
		}
	}

	public static class UsuarioJson {
		private UsuarioDTO usuario;

		public UsuarioJson() {

		}

		public UsuarioDTO getUsuario() {
			return this.usuario;
		}
	}
}