package org.kroz.activerecord;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;

/**
 * Base class for tables entities  
 * @author Vladimir Kroz
 * @author Wagied Davids
 * 
 * <p>This project based on and inspired by 'androidactiverecord' project written by JEREMYOT</p>
 */
public class ActiveRecordBase {

	static EntitiesMap s_EntitiesMap = new EntitiesMap();

	boolean m_NeedsInsert = true;
	Database m_Database;

	/**
	 * Creates new ActiveRecord instance. Returned instances is not initially
	 * opened. Calling application must explicitly open it by calling open()
	 * method
	 * 
	 * @param db
	 * @return
	 */
	static public ActiveRecordBase createInstance(Database db) {
		return new ActiveRecordBase(db);
	}

	/**
	 * Creates and opens new ActiveRecord object instance and underlying
	 * database. Returned ActiveRecord object is fully ready for use.
	 * 
	 * @param ctx
	 * @param dbName
	 * @return
	 * @throws ActiveRecordException
	 */
	static public ActiveRecordBase open(Context ctx, String dbName,
			int dbVersion) throws ActiveRecordException {
		Database db = Database.createInstance(ctx, dbName, dbVersion);
		db.open();
		return ActiveRecordBase.createInstance(db);
	}

	/**
	 * Opens ActiveRecord object and associated underlying database
	 * 
	 * @throws ActiveRecordException
	 */
	public void open() throws ActiveRecordException {
		m_Database.open();
	}
	
	/**
	 * Returns true is underlying database object is open
	 * @return
	 */
	public boolean isOpen() {
		return m_Database.isOpen();
	}

	/**
	 * Closes ActiveRecord object and associated underlying database
	 */
	public void close() {
		m_Database.close();
	}

	/**
	 * 
	 * @param db
	 */
	protected ActiveRecordBase(Database db) {
		m_Database = db;
	}

	protected ActiveRecordBase() {
	}

	/**
	 * Creates new entity instance connected with opened database
	 * 
	 * @param <T>
	 * @param type
	 *            The type of the required entity
	 * @return New entity instance
	 */
	public <T extends ActiveRecordBase> T newEntity(Class<T> type)
			throws ActiveRecordException {
		T entity = null;
		try {
			entity = type.newInstance();
			entity.setDatabase(m_Database);
		} catch (IllegalAccessException e) {
			throw new ActiveRecordException("Can't instantiate "
					+ type.getClass());
		} catch (InstantiationException e) {
			throw new ActiveRecordException("Can't instantiate "
					+ type.getClass());
		}
		return entity;
	}

	/**
	 * Copies values of fields from src to current object. Scans src object for
	 * the fields with the same names as in current object and copies it's
	 * valies. All fields are copied except special fields: 'id', 'created',
	 * 'modified', and also fields prefixed as 'm_*' and 's_' If src has fields
	 * not defiend in current object such fields are ignored
	 * 
	 * @param src
	 */
	public void copyFrom(Object src) {
		for (Field dstField : this.getColumnFieldsWithoutID()) {
			try {
				Field srcField = src.getClass().getField(dstField.getName());
				dstField.set(this, srcField.get(src));

			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}

	}

	/**
	 * Call this once at application launch, sets the database to use for
	 * AREntities.
	 * 
	 * @param database
	 *            The database to use.
	 */
	public void setDatabase(Database database) {
		m_Database = database;
	}

	protected long _id = 0;

	/**
	 * This entities row id.
	 * 
	 * @return The SQLite row id.
	 */
	public long getID() {
		return _id;
	}

	/**
	 * Get the table name for this class.
	 * 
	 * @return The table name for this class.
	 */
	protected String getTableName() {
		return CamelNotationHelper.toSQLName(getClass().getSimpleName());
	}

	/**
	 * Get this class's columns without the id column.
	 * 
	 * @return An array of the columns in this class's table.
	 */
	protected String[] getColumnsWithoutID() {
		List<String> columns = new ArrayList<String>();
		for (Field field : getColumnFieldsWithoutID()) {
			columns.add(field.getName());
		}
		return columns.toArray(new String[0]);
	}

	/**
	 * Get this class's columns.
	 * 
	 * @return An array of the columns in this class's table.
	 */
	protected String[] getColumns() {
		List<String> columns = new ArrayList<String>();
		for (Field field : getColumnFields()) {
			columns.add(field.getName());
		}
		return columns.toArray(new String[0]);
	}

	/**
	 * Get this class's fields without id.
	 * 
	 * @return An array of fields for this class.
	 */
	protected List<Field> getColumnFieldsWithoutID() {
		Field[] fields = getClass().getDeclaredFields();
		List<Field> columns = new ArrayList<Field>();
		for (Field field : fields) {
			if (!field.getName().startsWith("m_")
					&& !field.getName().startsWith("s_"))
				columns.add(field);
		}
		return columns;
	}

	/**
	 * Get this class's fields.
	 * 
	 * @return An array of fields for this class.
	 */
	protected List<Field> getColumnFields() {
		Field[] fields = getClass().getDeclaredFields();
		List<Field> columns = new ArrayList<Field>();
		for (Field field : fields) {
			if (!field.getName().startsWith("m_")
					&& !field.getName().startsWith("s_")) {
				columns.add(field);
			}
		}
		if (!getClass().equals(ActiveRecordBase.class)) {
			fields = ActiveRecordBase.class.getDeclaredFields();
			for (Field field : fields) {
				if (!field.getName().startsWith("m_")
						&& !field.getName().startsWith("s_")) {
					columns.add(field);
				}
			}
		}
		return columns;
	}

	/**
	 * Insert this entity into the database.
	 * @return the row ID of the newly inserted row, or -1 if an error occurred 
	 * @throws ActiveRecordException
	 */
	public long insert() throws ActiveRecordException {
		List<Field> columns = _id > 0 ? getColumnFields()
				: getColumnFieldsWithoutID();
		ContentValues values = new ContentValues(columns.size());
		for (Field column : columns) {
			try {
				if (column.getType().getSuperclass() == ActiveRecordBase.class)
					values.put(CamelNotationHelper.toSQLName(column.getName()),
							column.get(this) != null ? String
									.valueOf(((ActiveRecordBase) column
											.get(this))._id) : "0");
				else
					values.put(CamelNotationHelper.toSQLName(column.getName()),
							String.valueOf(column.get(this)));
			} catch (IllegalAccessException e) {
				throw new ActiveRecordException(e.getLocalizedMessage());
			}
		}
		_id = m_Database.insert(getTableName(), values);
		if (-1 != _id)
			m_NeedsInsert = false;
		
		return _id;
	}

	/**
	 * Update this entity in the database.
	 * @return The number of rows affected
	 * @throws NoSuchFieldException
	 */
	public int update() throws ActiveRecordException {
		List<Field> columns = getColumnFieldsWithoutID();
		ContentValues values = new ContentValues(columns.size());
		for (Field column : columns) {
			try {
				if (column.getType().getSuperclass() == ActiveRecordBase.class)
					values.put(CamelNotationHelper.toSQLName(column.getName()),
							column.get(this) != null ? String
									.valueOf(((ActiveRecordBase) column
											.get(this))._id) : "0");
				else
					values.put(CamelNotationHelper.toSQLName(column.getName()),
							String.valueOf(column.get(this)));
			} catch (IllegalArgumentException e) {
				throw new ActiveRecordException("No column " + column.getName());
			} catch (IllegalAccessException e) {
				throw new ActiveRecordException("No column " + column.getName());
			}
		}
		int r = m_Database.update(getTableName(), values, "_id = ?",
				new String[] { String.valueOf(_id) });
		return r;
	}

	/**
	 * Remove this entity from the database.
	 * 
	 * @return Whether or the entity was successfully deleted.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public boolean delete() throws ActiveRecordException {
		if (m_Database == null)
			throw new ActiveRecordException("Set database first");
		boolean toRet = m_Database.delete(getTableName(), "_id = ?",
				new String[] { String.valueOf(_id) }) != 0;
		_id = 0;
		m_NeedsInsert = true;
		return toRet;
	}

	/**
	 * Saves this entity to the database, inserts or updates as needed.
	 * @return number of rows affected on success, -1 on failure
	 * @throws ActiveRecordException
	 */
	public long save() throws ActiveRecordException {
		long r = -1;

		if (m_Database == null)
			throw new ActiveRecordException("Set database first");
		
		if (null == findByID(this.getClass(), _id))
			r = insert();
		else
			r = update();
		s_EntitiesMap.set(this);
		
		return r;
	}

	/**
	 * Inflate this entity using the current row from the given cursor.
	 * 
	 * @param cursor
	 *            The cursor to get object data from.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	void inflate(Cursor cursor) throws ActiveRecordException {
		HashMap<Field, Long> entities = new HashMap<Field, Long>();
		for (Field field : getColumnFields()) {
			try {
				String typeString = field.getType().getName();
				String colName = CamelNotationHelper.toSQLName(field.getName());
				if (typeString.equals("long")) {
					field.setLong(this, cursor.getLong(cursor
							.getColumnIndex(colName)));
				}
				else if (typeString.equals("java.lang.String")) {
					String val = cursor.getString(cursor.getColumnIndex(colName));
					field.set(this, val.equals("null") ? null : val);
				} else if (typeString.equals("double")) {
					field.setDouble(this, cursor.getDouble(cursor
							.getColumnIndex(colName)));
				}
				else if (typeString.equals("boolean")) {
					field.setBoolean(this, cursor.getString(
							cursor.getColumnIndex(colName)).equals(
							"true"));
				}
				else if (typeString.equals("[B")) {
					field.set(this, cursor.getBlob(cursor.getColumnIndex(colName)));
				}
				else if (typeString.equals("int")) {
					field.setInt(this, cursor.getInt(cursor
							.getColumnIndex(colName)));
				}
				else if (typeString.equals("float")) { 
					field.setFloat(this, cursor.getFloat(cursor
							.getColumnIndex(colName)));
				}
				else if (typeString.equals("short")) {
					field.setShort(this, cursor.getShort(cursor
							.getColumnIndex(colName)));
				}
				else if (typeString.equals("java.sql.Timestamp")) {
					long l = cursor.getLong(cursor.getColumnIndex(colName));
					field.set(this, new Timestamp(l));
				}
				else if (field.getType().getSuperclass() == ActiveRecordBase.class) {
					long id = cursor.getLong(cursor.getColumnIndex(colName));
					if (id > 0)
						entities.put(field, id);
					else
						field.set(this, null);
				} else
					throw new ActiveRecordException(
							"Class cannot be read from Sqlite3 database.");
			} catch (IllegalArgumentException e) {
				throw new ActiveRecordException(e.getLocalizedMessage());
			} catch (IllegalAccessException e) {
				throw new ActiveRecordException(e.getLocalizedMessage());
			}

		}

		s_EntitiesMap.set(this);
		for (Field f : entities.keySet()) {
			try {
				f.set(this, this.findByID((Class<? extends ActiveRecordBase>) f
						.getType(), entities.get(f)));
			} catch (SQLiteException e) {
				throw new ActiveRecordException(e.getLocalizedMessage());
			} catch (IllegalArgumentException e) {
				throw new ActiveRecordException(e.getLocalizedMessage());
			} catch (IllegalAccessException e) {
				throw new ActiveRecordException(e.getLocalizedMessage());
			}
		}
	}

	/**
	 * Delete selected entities from the database.
	 * 
	 * @param <T>
	 *            Any AREntity class.
	 * @param type
	 *            The class of the entities to delete.
	 * @param whereClause
	 *            The condition to match (Don't include "where").
	 * @param whereArgs
	 *            The arguments to replace "?" with.
	 * @return The number of rows affected.
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public <T extends ActiveRecordBase> int delete(Class<T> type,
			String whereClause, String[] whereArgs)
			throws ActiveRecordException {
		if (m_Database == null)
			throw new ActiveRecordException("Set database first");
		T entity;
		try {
			entity = type.newInstance();
		} catch (IllegalAccessException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		} catch (InstantiationException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		}
		return m_Database.delete(entity.getTableName(), whereClause, whereArgs);
	}

	/**
	 * Delete all instances of an entity from the database where a column has a
	 * specified value.
	 * 
	 * @param <T>
	 *            Any AREntity class.
	 * @param type
	 *            The class of the entities to delete.
	 * @param column
	 *            The column to match.
	 * @param value
	 *            The value required for deletion.
	 * @return The number of rows affected.
	 * @throws ActiveRecordException
	 */
	public <T extends ActiveRecordBase> int deleteByColumn(Class<T> type,
			String column, String value) throws ActiveRecordException {
		return delete(type, String.format("%s = ?", column),
				new String[] { value });
	}

	/**
	 * Return all instances of an entity that match the given criteria.
	 * 
	 * @param <T>
	 *            Any ActiveRecordBase class.
	 * @param type
	 *            The class of the entities to return.
	 * @param whereClause
	 *            The condition to match (Don't include "where").
	 * @param whereArgs
	 *            The arguments to replace "?" with.
	 * @return A generic list of all matching entities.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public <T extends ActiveRecordBase> List<T> find(Class<T> type,
			String whereClause, String[] whereArgs)
			throws ActiveRecordException {
		if (m_Database == null)
			throw new ActiveRecordException("Set database first");
		T entity = null;
		try {
			entity = type.newInstance();
		} catch (IllegalAccessException e1) {
			throw new ActiveRecordException(e1.getLocalizedMessage());
		} catch (InstantiationException e1) {
			throw new ActiveRecordException(e1.getLocalizedMessage());
		}
		List<T> toRet = new ArrayList<T>();
		Cursor c = m_Database.query(entity.getTableName(), null, whereClause,
				whereArgs);
		try {
			while (c.moveToNext()) {
				entity = s_EntitiesMap.get(type, c.getLong(c
						.getColumnIndex("_id")));
				if (entity == null) {
					entity = type.newInstance();
					entity.m_NeedsInsert = false;
					entity.inflate(c);
					entity.m_Database = m_Database;
					
				}
				toRet.add(entity);
			}
		} catch (IllegalAccessException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		} catch (InstantiationException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		} finally {
			c.close();
		}
		return toRet;
	}

	/**
	 * Return all instances of an entity that match the given criteria.
	 * 
	 * @param <T>
	 *            Any ActiveRecordBase class.
	 * @param type
	 *            The class of the entities to return.
	 * @param distinct
	 * @param whereClause
	 *            The condition to match (Don't include "where").
	 * @param whereArgs
	 *            The arguments to replace "?" with.
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @param limit
	 * @return A generic list of all matching entities.
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public <T extends ActiveRecordBase> List<T> find(Class<T> type,
			boolean distinct, String whereClause, String[] whereArgs, String groupBy, String having, 
			String orderBy, String limit) throws ActiveRecordException {
		if (m_Database == null)
			throw new ActiveRecordException("Set database first");
		T entity;
		try {
			entity = type.newInstance();
		} catch (IllegalAccessException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		} catch (InstantiationException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		}
		List<T> toRet = new ArrayList<T>();
		Cursor c = m_Database.query(distinct, entity.getTableName(), null,
				whereClause, whereArgs, groupBy, having, orderBy, limit);
		try {
			while (c.moveToNext()) {
				entity = s_EntitiesMap.get(type, c.getLong(c
						.getColumnIndex("_id")));
				if (entity == null) {
					entity = type.newInstance();
					entity.m_NeedsInsert = false;
					entity.inflate(c);
					entity.m_Database = m_Database;
					
				}
				toRet.add(entity);
			}
		} catch (IllegalAccessException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		} catch (InstantiationException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		} finally {
			c.close();
		}
		return toRet;
	}

	/**
	 * Return all instances of an entity from the database where a column has a
	 * specified value.
	 * 
	 * @param <T>
	 *            Any ActiveRecordBase class.
	 * @param type
	 *            The class of the entities to return.
	 * @param column
	 *            The tables's column to match. Note - it must be name from DB schema, not Java field name 
	 * @param value
	 *            The desired value.
	 * @return A generic list of all matching entities.
	 * @throws ActiveRecordException
	 */
	public <T extends ActiveRecordBase> List<T> findByColumn(Class<T> type,
			String column, String value) throws ActiveRecordException {
		return find(type, String.format("%s = ?", column),
				new String[] { value });
	}

	/**
	 * Return the instance of an entity with a matching id.
	 * 
	 * @param <T>
	 *            Any ActiveRecordBase class.
	 * @param type
	 *            The class of the entity to return.
	 * @param id
	 *            The desired ID.
	 * @return The matching entity if reocrd found in DB, null otherwise
	 * @throws ActiveRecordException
	 */
	public <T extends ActiveRecordBase> T findByID(Class<T> type, long id)
			throws ActiveRecordException {
		if (m_Database == null)
			throw new ActiveRecordException("Set database first");
		T entity = s_EntitiesMap.get(type, id);
		if (entity != null)
			return entity;

		try {
			entity = type.newInstance();
		} catch (IllegalAccessException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		} catch (InstantiationException e) {
			throw new ActiveRecordException(e.getLocalizedMessage());
		}

		Cursor c = m_Database.query(entity.getTableName(), null, "_id = ?",
				new String[] { String.valueOf(id) });
		try {
			if (!c.moveToNext()) {
				return null;
			} else {
				entity.inflate(c);
				entity.m_NeedsInsert = false;
				entity.m_Database = m_Database;
			}
		} finally {
			c.close();
		}
		return entity;
	}

	/**
	 * Return all instances of an entity from the database.
	 * 
	 * @param <T>
	 *            Any ActiveRecordBase class.
	 * @param type
	 *            The class of the entities to return.
	 * @return A generic list of all matching entities.
	 * @throws ActiveRecordException
	 */
	public <T extends ActiveRecordBase> List<T> findAll(Class<T> type)
			throws ActiveRecordException {
		return find(type, null, null);
	}

}
