package org.mirapolis.service.grid;

import org.mirapolis.data.DataSet;
import org.mirapolis.data.DataSetHelper;
import org.mirapolis.data.bean.BeanHelper;
import org.mirapolis.data.bean.reflect.AbstractReflectDataBean;
import org.mirapolis.data.bean.reflect.VirtualReflectDataBean;
import org.mirapolis.data.bean.reflect.virtual.IdBooleanVirtualBean;
import org.mirapolis.data.bean.reflect.virtual.IdVirtualBean;
import org.mirapolis.mvc.model.entity.EntityListenerService;
import org.mirapolis.mvc.model.grid.AbstractDataGrid;
import org.mirapolis.mvc.model.grid.VirtualDataSource;
import org.mirapolis.mvc.model.grid.bean.BeanDataSetList;
import org.mirapolis.mvc.model.grid.bean.VirtualBeanDataSource;
import org.mirapolis.mvc.model.grid.bean.VirtualIDBean;
import org.mirapolis.service.ServiceFactory;
import org.mirapolis.util.BooleanHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис для работы с ручным выбором в гридах
 *
 * @author Artem Kuznetsov
 * @since 21.08.2018
 */
@Service
public class GridChoosingService {

	private final EntityListenerService entityListenerService;

	public GridChoosingService(EntityListenerService entityListenerService) {
		this.entityListenerService = entityListenerService;
	}

	public static GridChoosingService getInstance() {
		return ServiceFactory.getService(GridChoosingService.class);
	}

	/**
	 * В качестве ключа, по которому получаем id из {@code dataSets} используем {@link AbstractDataGrid#ID}
	 * @see #updateDataSetsChoosingColumn(List, String, String, String)
	 *
	 * Нужно работать с виртуальными данными через VirtualBeanDataSource
	 * Использовать {@link #updateVirtualBeansChoosingColumn(List, String, Function, BiConsumer)}
	 */
	@Deprecated
	public void updateDataSetsChoosingColumn(
		List<DataSet> dataSets,
		String virtualDataSourceName,
		String choosingColumnName
	) {
		updateDataSetsChoosingColumn(dataSets, virtualDataSourceName, choosingColumnName, AbstractDataGrid.ID);
	}

	/**
	 * Добавить значение для колонки {@code choosingColumnName} выбора в наборы данных {@code dataSets},
	 * id выбранных объектов берем из {@link VirtualDataSource} по ключу {@code virtualDataSourceName}
	 * @param virtualDataSourceName ключ, по которому получаем id выбранных объектов из {@link VirtualDataSource}
	 * @see #updateDataSetsChoosingColumn(List, Set, String, String)
	 *
	 * Нужно работать с виртуальными данными через VirtualBeanDataSource
	 * Использовать {@link #updateVirtualBeansChoosingColumn(List, String, Function, BiConsumer)}
	 */
	@Deprecated
	public void updateDataSetsChoosingColumn(
		List<DataSet> dataSets,
		String virtualDataSourceName,
		String choosingColumnName,
		String idColumnName
	) {
		updateDataSetsChoosingColumn(
			dataSets,
			getChosenIdsFromVirtualGrid(virtualDataSourceName),
			choosingColumnName,
			idColumnName
		);
	}

	/**
	 * Добавить значение для колонки {@code choosingColumnName} выбора в наборы данных {@code dataSets}
	 * @param dataSets наборы данных, куда добавляем значение колонки
	 * @param chosenIds id выбранных объектов
	 * @param choosingColumnName название колонки выбора
	 * @param idColumnName название колонки, по которому достаем id из dataSet
	 *
	 * Нельзя использовать - см. AbstractBeanDataGrid#updateGridDataSet
	 * Использовать {@link #updateBeansChoosingColumn(List, Set, Function, BiConsumer)}
	 */
	@Deprecated
	public void updateDataSetsChoosingColumn(
		List<DataSet> dataSets,
		Set<String> chosenIds,
		String choosingColumnName,
		String idColumnName
	) {
		dataSets.forEach(dataSet ->
			updateDataSetChoosingColumn(dataSet, chosenIds, choosingColumnName, dataSet.getValue(idColumnName))
		);
	}

	/**
	 * Добавить значение для колонки {@code choosingColumnName} выбора в {@code dataSet}
	 * @param dataSet набор данных, куда добавляем значение колонки
	 * @param chosenIds id выбранных объектов
	 * @param choosingColumnName название колонки выбора
	 * @param id id объекта
	 */
	public void updateDataSetChoosingColumn(
		DataSet dataSet,
		Set<String> chosenIds,
		String choosingColumnName,
		String id
	) {
		dataSet.putValue(choosingColumnName, BooleanHelper.getNumericValue(chosenIds.contains(id)));
	}

	/**
	 * @param virtualDataSourceName название набора данных, откуда берем выбранные id
	 * @see #updateBeansChoosingColumn(List, VirtualDataSource, Function, BiConsumer)
	 *
	 * Нужно работать с виртуальными данными через VirtualBeanDataSource
	 * Использовать {@link #updateVirtualBeansChoosingColumn(List, String, Function, BiConsumer)}
	 */
	@Deprecated
	public <T extends AbstractReflectDataBean> void updateBeansChoosingColumn(
		List<T> beans,
		String virtualDataSourceName,
		Function<T, String> idGetter,
		BiConsumer<T, Boolean> choosingSetter
	) {
		updateBeansChoosingColumn(beans, getChosenIdsFromVirtualGrid(virtualDataSourceName), idGetter, choosingSetter);
	}

	/**
	 * @param virtualDataSourceName название набора виртуальных бинов, откуда берем выбранные {@link VirtualIDBean#ID}
	 * @see #updateBeansChoosingColumn(List, VirtualDataSource, Function, BiConsumer)
	 */
	public <T extends VirtualReflectDataBean> void updateVirtualIDBeansChoosingColumn(
		List<T> beans,
		String virtualDataSourceName,
		Function<T, String> idGetter,
		BiConsumer<T, Boolean> choosingSetter
	) {
		updateBeansChoosingColumn(
			beans,
			getChosenIdsFromVirtualIDBeanGrid(virtualDataSourceName),
			idGetter,
			choosingSetter
		);
	}

	/**
	 * @param virtualBeanDataSourceName название набора виртуальных бинов, откуда берем выбранные id
	 * @see #updateBeansChoosingColumn(List, VirtualDataSource, Function, BiConsumer)
	 */
	public <T extends VirtualReflectDataBean> void updateVirtualBeansChoosingColumn(
		List<T> beans,
		String virtualBeanDataSourceName,
		Function<T, String> idGetter,
		BiConsumer<T, Boolean> choosingSetter
	) {
		updateBeansChoosingColumn(
			beans,
			getChosenIdsFromVirtualBeanGrid(virtualBeanDataSourceName),
			idGetter,
			choosingSetter
		);
	}

	/**
	 * id выбранных объектов берем из виртуального набора данных {@code virtualDataSource}
	 * @param virtualDataSource набор данных, откуда берем
	 * @see #updateBeansChoosingColumn(List, Set, Function, BiConsumer)
	 *
	 * Нужно работать с виртуальными данными через VirtualBeanDataSource
	 * Использовать {@link #updateVirtualBeansChoosingColumn(List, VirtualBeanDataSource, Function, BiConsumer)}
	 */
	@Deprecated
	public <T extends AbstractReflectDataBean> void updateBeansChoosingColumn(
		List<T> beans,
		VirtualDataSource virtualDataSource,
		Function<T, String> idGetter,
		BiConsumer<T, Boolean> choosingSetter
	) {
		updateBeansChoosingColumn(beans, getChosenIdsFromVirtualGrid(virtualDataSource), idGetter, choosingSetter);
	}

	/**
	 * id выбранных объектов берем из набора данных виртуальных бинов {@code virtualBeanDataSource}
	 * @param virtualBeanDataSource набор данных, откуда берем
	 * @see #updateBeansChoosingColumn(List, Set, Function, BiConsumer)
	 */
	public <T extends VirtualReflectDataBean> void updateVirtualBeansChoosingColumn(
		List<T> beans,
		VirtualBeanDataSource<T> virtualBeanDataSource,
		Function<T, String> idGetter,
		BiConsumer<T, Boolean> choosingSetter
	) {
		updateBeansChoosingColumn(
			beans,
			getChosenIdsFromVirtualBeanGrid(virtualBeanDataSource),
			idGetter,
			choosingSetter
		);
	}

	/**
	 * Добавить в бины {@code beans} значения переменной выбора
	 * @param beans набор бинов, для которых добавляем значение
	 * @param chosenIds id выбранных объектов
	 * @param idGetter ф-я получения id из бина
	 * @param choosingSetter ф-я присваивания значения выбора
	 * @param <T> класс бина
	 */
	public <T extends AbstractReflectDataBean> void updateBeansChoosingColumn(
		List<T> beans,
		Set<String> chosenIds,
		Function<T, String> idGetter,
		BiConsumer<T, Boolean> choosingSetter
	) {
		beans.forEach(bean -> choosingSetter.accept(bean, chosenIds.contains(idGetter.apply(bean))));
	}

	/**
	 * В качестве название колонки, в которой лежат выбранные id, используем {@link AbstractDataGrid#ID}
	 * @see #getChosenIdsFromVirtualGrid(String, String)
	 *
	 * Нужно работать с виртуальными данными через VirtualBeanDataSource
	 * Использовать {@link #getChosenIdsFromVirtualBeanGrid(String)}
	 */
	@Deprecated
	public Set<String> getChosenIdsFromVirtualGrid(String virtualDataSourceName) {
		return getChosenIdsFromVirtualGrid(virtualDataSourceName, AbstractDataGrid.ID);
	}

	/**
	 * В качестве название колонки, в которой лежат выбранные id, используем {@link AbstractDataGrid#ID}
	 * @see #getChosenIdsFromVirtualBeanGrid(String, String)
	 */
	public Set<String> getChosenIdsFromVirtualBeanGrid(String virtualBeanDataSourceName) {
		return getChosenIdsFromVirtualBeanGrid(virtualBeanDataSourceName, AbstractDataGrid.ID);
	}

	/**
	 * В качестве название колонки, в которой лежат выбранные id, используем {@link VirtualIDBean#ID}
	 * @see #getChosenIdsFromVirtualBeanGrid(String, String)
	 */
	public Set<String> getChosenIdsFromVirtualIDBeanGrid(String virtualBeanDataSourceName) {
		return getChosenIdsFromVirtualBeanGrid(virtualBeanDataSourceName, VirtualIDBean.ID);
	}

	/**
	 * @see #getChosenIdsFromVirtualGrid(VirtualDataSource, String)
	 *
	 * Нужно работать с виртуальными данными через VirtualBeanDataSource
	 * Использовать {@link #getChosenIdsFromVirtualBeanGrid(String, String)}
	 */
	@Deprecated
	public Set<String> getChosenIdsFromVirtualGrid(String virtualDataSourceName, String idColumnName) {
		return getChosenIdsFromVirtualGrid(VirtualDataSource.get(virtualDataSourceName), idColumnName);
	}

	/**
	 * @see #getChosenIdsFromVirtualBeanGrid(VirtualBeanDataSource, String)
	 */
	public Set<String> getChosenIdsFromVirtualBeanGrid(String virtualBeanDataSourceName, String idColumnName) {
		return getChosenIdsFromVirtualBeanGrid(VirtualBeanDataSource.get(virtualBeanDataSourceName), idColumnName);
	}

	/**
	 * В качестве название колонки, в которой лежат выбранные id, используем {@link AbstractDataGrid#ID}
	 * @see #getChosenIdsFromVirtualGrid(VirtualDataSource, String)
	 *
	 * Нужно работать с виртуальными данными через VirtualBeanDataSource
	 * Использовать {@link #getChosenIdsFromVirtualBeanGrid(VirtualBeanDataSource)}
	 */
	@Deprecated
	public Set<String> getChosenIdsFromVirtualGrid(VirtualDataSource virtualDataSource) {
		return getChosenIdsFromVirtualGrid(virtualDataSource, AbstractDataGrid.ID);
	}

	/**
	 * В качестве название колонки, в которой лежат выбранные id, используем {@link AbstractDataGrid#ID}
	 * @see #getChosenIdsFromVirtualBeanGrid(VirtualBeanDataSource, String)
	 */
	public <T extends VirtualReflectDataBean> Set<String> getChosenIdsFromVirtualBeanGrid(
		VirtualBeanDataSource<T> virtualBeanDataSource
	) {
		return getChosenIdsFromVirtualBeanGrid(virtualBeanDataSource, AbstractDataGrid.ID);
	}

	/**
	 * Возвращает набор выбранных id из виртуального набора данных {@code virtualDataSource}
	 * @param virtualDataSource ключ, по которому получаем выбранные id из {@link VirtualDataSource}
	 * @param idColumnName название колонки, в которой лежат выбранные id
	 *
	 * Нужно работать с виртуальными данными через VirtualBeanDataSource
	 * Использовать {@link #getChosenIdsFromVirtualBeanGrid(VirtualBeanDataSource, String)}
	 */
	@Deprecated
	public Set<String> getChosenIdsFromVirtualGrid(VirtualDataSource virtualDataSource, String idColumnName) {
		return DataSetHelper.getValuesSet(virtualDataSource.getRecords(), idColumnName);
	}

	/**
	 * Возвращает набор выбранных id из набора данных виртуальных бинов {@code virtualBeanDataSource}
	 * @param virtualBeanDataSource ключ, по которому получаем выбранные id из {@link VirtualBeanDataSource}
	 * @param idColumnName название колонки, в которой лежат выбранные id
	 */
	public <T extends VirtualReflectDataBean> Set<String> getChosenIdsFromVirtualBeanGrid(
		VirtualBeanDataSource<T> virtualBeanDataSource,
		String idColumnName
	) {
		return BeanHelper.getValueSet(virtualBeanDataSource.getBeans(), idColumnName);
	}

	/**
	 * Возвращает мапу (id объекта -> флаг отметки объекта (true/false))из набора данных виртуальных бинов
	 *
	 * @param virtualBeanDataSourceName название набора виртуальных бинов
	 */
	public <T extends IdBooleanVirtualBean> Map<String, Boolean> getMarkedIdsMapFromVirtualBeanGrid(
		String virtualBeanDataSourceName
	) {
		return VirtualBeanDataSource.<T>get(virtualBeanDataSourceName)
			.getBeans()
			.stream()
			.collect(Collectors.toMap(
				IdVirtualBean::getId,
				IdBooleanVirtualBean::getValue
			));
	}

	/**
	 * Удаляет сущности выбранные через чек - бокс на гриде
	 *
	 * @param choosingGridName имя грида {@link VirtualDataSource} на котором был добавлен action с чек боксом
	 * @param entityType       имя фрейма, который привязан к сущности
	 */
	public void deleteChoosingEntity(String choosingGridName, String entityType) {
		deleteChoosingEntity(getChosenIdsFromVirtualGrid(choosingGridName, BeanDataSetList.ID), entityType);
	}

	/**
	 * Удаляет сущности выбранные через чек - бокс на гриде
	 *
	 * @param choosingGridName имя грида {@link VirtualBeanDataSource} на котором был добавлен action с чек боксом
	 * @param entityType       имя фрейма, который привязан к сущности
	 */
	public void deleteIDBeansChoosingEntity(String choosingGridName, String entityType) {
		deleteChoosingEntity(getChosenIdsFromVirtualIDBeanGrid(choosingGridName), entityType);
	}

	private void deleteChoosingEntity(Set<String> chosenIds, String entityType) {
		chosenIds.forEach(id -> entityListenerService.delete(entityType, id));
	}
}
