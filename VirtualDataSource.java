package org.mirapolis.mvc.model.grid;

import org.mirapolis.core.Application;
import org.mirapolis.data.DataSet;
import org.mirapolis.data.SimpleDataSet;
import org.mirapolis.data.bean.reflect.AbstractReflectDataBean;
import org.mirapolis.mvc.model.table.NameColumn;
import org.mirapolis.orm.DataField;
import org.mirapolis.util.StringHelper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mirapolis.util.CollectionUtils.array;

/**
 * Виртуальный набор данных
 * @author Usov Andrey
 * @since 18.01.2007
 */
public class VirtualDataSource extends AbstractVirtualDataSource<DataSet> {

	public void addRecord(AbstractReflectDataBean bean) {
		addRecord(addNameColumn(bean));
	}

	public void addRecord(DataSet record) {
		synchronized (dataList) {
			dataList.add(record);
		}
	}

	public void addRecord(String id, String type, String name) {
		DataSet dataSet = new SimpleDataSet();
		dataSet.put("id", id);
		dataSet.put("type", type);
		dataSet.put("name", name);
		addRecord(dataSet);
	}

	public void addRecords(Collection<DataSet> records) {
		synchronized (dataList) {
			dataList.addAll(records);
		}
	}

	/**
	 * @deprecated use insertRecord(ReflectDataBean bean)
	 */
	public void insertRecord(DataSet record) {
		synchronized (dataList) {
			dataList.add(0, record);
		}
	}

	public void setBeans(List<? extends AbstractReflectDataBean> beans) {
		synchronized (dataList) {
			dataList.clear();
			for (AbstractReflectDataBean bean : beans) {
				dataList.add(addNameColumn(bean));
			}
		}
	}

	/**
	 * @deprecated user setRecords(List<ReflectDataBean> beans)
	 */
	public void setRecords(List<DataSet> records) {
		synchronized (dataList) {
			dataList.clear();
			dataList.addAll(records);
		}
	}

	public boolean isExists(String[] fields, String[] values) {
		synchronized (dataList) {
			return isExistsUnsafe(fields, values);
		}
	}

	public boolean isExists(List<String> fields, List<String> value) {
		synchronized (dataList) {
			boolean exists = true;
			for (DataSet intRecord : dataList) {
				for (int i=0; i< fields.size(); i++) {
					String intValue = intRecord.getValue(fields.get(i));
					if (intValue == null ||  !intValue.equals(value.get(i))){
						exists = false;
					}
				}
			}
			return exists;
		}
	}

	private boolean isExistsUnsafe(String[] fields, String[] values) {
		for (DataSet intRecord : dataList) {
			boolean isExists = true;
			for (int i = 0; i < fields.length; i++) {
				String name = fields[i];
				String intValue = intRecord.getValue(name);
				if (intValue == null || !intValue.equals(values[i])) {
					isExists = false;
				}
			}
			if (isExists) {
				return true;
			}
		}
		return false;
	}

	public boolean isExists(String field, String value) {
		return isExists(array(field), array(value));
	}

	/**
	 * Добавляет в {@link AbstractVirtualDataSource#dataList} набор данных
	 * с ключами из {@code fieldNames} и значениями bp {@code values}, если данный набор
	 * данных еще не существует
	 * @param fieldNames - имена полей
	 * @param values - значения полей в, порядок значений
	 *                  соответствует порядку имен полей {@code fieldNames}
	 */
	public void addRecord(String[] fieldNames, String[] values) {
		synchronized (dataList) {
			if (isExistsUnsafe(fieldNames, values)) {
				return;
			}
			DataSet dataSet = new SimpleDataSet();
			for (int i = 0; i < fieldNames.length; i++) {
				dataSet.putValue(fieldNames[i], values[i]);
			}
			dataList.add(dataSet);
		}
	}

	/**
	 * @deprecated use {@link this#removeRecord(List, List)}
	 */
	public void removeRecord(String[] fieldNames, String[] values) {
		removeRecord(Arrays.asList(fieldNames), Arrays.asList(values));
	}

	/**
	 * Удаляет запись из {@link AbstractVirtualDataSource#dataList}
	 * по индексу найденному в {@link VirtualDataSource#findFirstRecordIndex(List, List)}
	 * Если подходящая под условия запись не найдена, удалено ни чего не будет.
	 */
	public void removeRecord(List<String> fieldNames, List<String> values) {
		synchronized (dataList) {
			findFirstRecordIndex(fieldNames, values)
				.ifPresent(dataList::remove);
		}
	}

	/**
	 * Удаляет запись из {@link AbstractVirtualDataSource#dataList}
	 * по индексу найденному в {@link VirtualDataSource#findFirstRecordIndex(List, List)}
	 * Если подходящая под условия запись не найдена, удалено ни чего не будет.
	 */
	public void removeRecord(String fieldName, String value) {
		removeRecord(Collections.singletonList(fieldName), Collections.singletonList(value));
	}

	/**
	 * Удаляет запись из данного VirtualDataSource
	 */
	public void removeRecord(DataSet record) {
		synchronized (dataList) {
			dataList.remove(record);
		}
	}

	/**
	 * Удаляет все записи из {@link AbstractVirtualDataSource#dataList}
	 * соответствующие предикату {@link VirtualDataSource#isCorrectRecord(List, List, DataSet)}
	 */
	public void removeFullRecord(List<String> fieldNames, List<String> values) {
		synchronized (dataList) {
			dataList.removeIf(dataSet -> isCorrectRecord(fieldNames, values, dataSet));
		}
	}

	/**
	 * Удаляет все записи из {@link AbstractVirtualDataSource#dataList}
	 * соответствующие предикату {@link VirtualDataSource#isCorrectRecord(List, List, DataSet)}
	 */
	public void removeFullRecord(String fieldNames, String values) {
		removeFullRecord(Collections.singletonList(fieldNames), Collections.singletonList(values));
	}

	/**
	 * Возвращает индекс первой запись из {@link AbstractVirtualDataSource#dataList}
	 * у которой все значения полей {@code fieldNames} соответствуют переданным значениям {@code values}.
	 * Порядок значений в {@code values} соответствует порядку имен полей в {@code fieldNames}.
	 */
	private OptionalInt findFirstRecordIndex(List<String> fieldNames, List<String> values) {
		return IntStream.range(0, dataList.size())
			.filter(i -> isCorrectRecord(fieldNames, values, dataList.get(i)))
			.findFirst();
	}

	/**
	 * Проверяет, соответствуют ли все значения полей {@code fieldNames} из {@code record}
	 * переданным значениям {@code values}
	 *
	 * @param fieldNames - имена полей в {@code record}
	 * @param values     - требуемые значения полей в {@code record},
	 *                   порядок значений соответствует порядку имен полей {@code fieldNames}
	 * @param record     - запись из набора данных {@link AbstractVirtualDataSource#dataList}
	 */
	private boolean isCorrectRecord(List<String> fieldNames, List<String> values, DataSet record) {
		return IntStream.range(0, fieldNames.size())
			.allMatch(i -> values.get(i).equals(record.getValue(fieldNames.get(i))));
	}

	/**
	 * Добавляет в DataSet поле 'name' для корректной работы фильтров. Выбирает первое name поле из списка DataField
	 */
	private DataSet addNameColumn(AbstractReflectDataBean bean) {
		DataSet dataSet = bean.get();
		if (!dataSet.containsKey(NameColumn.COLUMN_NAME)) {
			bean.fields().filter(DataField::isName).findFirst().ifPresent(nameField ->
				dataSet.putValue(NameColumn.COLUMN_NAME, StringHelper.defaultIfNull(bean.get(nameField.getName())))
			);
		}
		return dataSet;
	}

	public List<DataSet> getRecords() {
		return Collections.unmodifiableList(dataList);
	}

	/**
	 * Очищает список записей
	 */
	public void clearRecords() {
		synchronized (dataList) {
			dataList.clear();
		}
	}

    /**
     * Возвращает первую запись из списка записей и удаляет ее
     */
    public DataSet pull() {
    	synchronized (dataList) {
			return dataList.remove(0);
		}
    }

	public static VirtualDataSource get(String gridName) {
		DataSourceCreator<VirtualDataSource> dataSourceCreator = getDataSourceCreator(gridName);
		return dataSourceCreator.getOrCreateDataSource();
	}

	@Override
	protected DataSet extractDataSet(DataSet value) {
		return value;
	}

	public boolean isFree() {
    	synchronized (dataList) {
			return dataList.isEmpty();
		}
	}

	private static DataSourceCreator<VirtualDataSource> getDataSourceCreator(String gridName) {
		return new DataSourceCreator<VirtualDataSource>(getInternalName(gridName)) {

			@Override
			protected VirtualDataSource createDataSource() {
				Application.log.debug("Create New Virtual Grid: " + internalGridName);
				return new VirtualDataSource();
			}
		};
	}

	@Override
	protected void sort(List<Sort> sorts, List<DataSet> list) {
		list.sort(list.get(0).getComparator(sorts));
	}

    /**
     * @param fieldName имя поля
     * @return сет значений поля из записей в наборе данных
     */
    public Set<String> getRecordsValues(String fieldName) {
        return getRecords().stream().map(dataSet -> dataSet.getValue(fieldName)).collect(Collectors.toSet());
    }
}
