package brix.plugin.site.page.admin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.lang.Objects;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.page.AbstractContainer;
import brix.plugin.site.page.global.GlobalContainerNode;
import brix.web.generic.BrixGenericPanel;

import com.inmethod.grid.IDataSource;
import com.inmethod.grid.IGridColumn;
import com.inmethod.grid.SizeUnit;
import com.inmethod.grid.column.CheckBoxColumn;
import com.inmethod.grid.column.PropertyColumn;
import com.inmethod.grid.column.editable.EditablePropertyColumn;
import com.inmethod.grid.column.editable.SubmitCancelColumn;
import com.inmethod.grid.datagrid.DataGrid;

public class VariablesPanel extends BrixGenericPanel<BrixNode>
{
	private AjaxLink<?> delete;
	
	public VariablesPanel(String id, IModel<BrixNode> model)
	{
		super(id, model);
		
		List<IGridColumn> columns = new ArrayList<IGridColumn>();
		columns.add(new CheckBoxColumn("checkbox"));
		columns.add(new PropertyColumn(new ResourceModel("key"), "key"));
		columns.add(new EditablePropertyColumn(new ResourceModel("value"), "value") {
			@Override
			protected void addValidators(FormComponent component)
			{
				component.setRequired(true);
			}
		});
		columns.add(new SubmitCancelColumn("submitcancel", new ResourceModel("edit")) {
			@Override
			protected void onError(AjaxRequestTarget target, IModel rowModel, WebMarkupContainer rowComponent)
			{
				target.addChildren(VariablesPanel.this, FeedbackPanel.class);
			}
			@Override
			protected void onSubmitted(AjaxRequestTarget target, IModel rowModel, WebMarkupContainer rowComponent)
			{
				target.addChildren(VariablesPanel.this, FeedbackPanel.class);
				super.onSubmitted(target, rowModel, rowComponent);
			}
		});
							
		final DataGrid grid = new DataGrid("grid", new DataSource(), columns) {
			@Override
			public void onItemSelectionChanged(IModel item, boolean newValue)
			{
				AjaxRequestTarget target = AjaxRequestTarget.get();
				if (target != null)
				{
					target.addComponent(delete);
				}
				super.onItemSelectionChanged(item, newValue);
			}
		};
		add(grid);
		grid.setSelectToEdit(false);
		grid.setClickRowToSelect(true);
		grid.setContentHeight(17, SizeUnit.EM);

		add(delete = new AjaxLink<Void>("deleteSelected") {
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				AbstractContainer node = (AbstractContainer) VariablesPanel.this.getModelObject();
				for (IModel<?> m : grid.getSelectedItems())
				{
					Entry e = (Entry) m.getObject();
					node.setVariableValue(e.getKey(), null);
				}
				node.save();
				grid.markAllItemsDirty();
				grid.update();
				grid.resetSelectedItems();
				target.addComponent(this);
			}
			@Override
			public boolean isEnabled()
			{
				return grid.getSelectedItems().isEmpty() == false;
			}
		});
		delete.setOutputMarkupId(true);
		
		add(new InsertForm("form")
		{
			@Override
			protected void onItemAdded()
			{
				grid.markAllItemsDirty();
				grid.update();
			}
		});

		add(new FeedbackPanel("feedback").setOutputMarkupId(true));
	}

	private class DataSource implements IDataSource
	{

		public IModel model(Object object)
		{
			return new Model((Serializable) object);
		}

		public void query(IQuery query, IQueryResult result)
		{
			AbstractContainer node = (AbstractContainer) VariablesPanel.this.getModelObject();
			List<Entry> res = new ArrayList<Entry>();
			for (String s : node.getSavedVariableKeys())
			{
				res.add(new Entry(s));
			}
			Collections.sort(res, new Comparator<Entry>() {

				public int compare(Entry o1, Entry o2)
				{
					return o1.getKey().compareTo(o2.getKey());
				}				
			});
			result.setItems(res.iterator());
			result.setTotalCount(res.size());
		}

		public void detach()
		{		
		}		
	};
	
	private class Entry implements Serializable
	{
		private final String key;

		public Entry(String key)
		{
			this.key = key;
		}

		public String getKey()
		{
			return key;
		}

		public String getValue()
		{
			AbstractContainer node = (AbstractContainer) VariablesPanel.this.getModelObject();
			return node.getVariableValue(key, false);
		}

		public void setValue(String value)
		{
			AbstractContainer node = (AbstractContainer) VariablesPanel.this.getModelObject();
			node.setVariableValue(key, value);
			node.save();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj instanceof Entry == false)
				return false;
			Entry that = (Entry) obj;
			return Objects.equal(key, that.key);			
		}
		
		@Override
		public int hashCode()
		{
			return Objects.hashCode(key);
		}
	};

	private abstract class InsertForm extends Form<Void>
	{

		public InsertForm(String id)
		{
			super(id);

			IModel<List<? extends String>> choicesModel = new LoadableDetachableModel<List<? extends String>>()
			{
				@Override
				protected List<? extends String> load()
				{
					List<String> result = new ArrayList<String>();
					AbstractContainer node = (AbstractContainer) VariablesPanel.this.getModelObject();
					result.addAll(node.getVariableKeys());
					return result;
				}
			};

			final TextField<String> tf;
			add(tf = new TextField<String>("key", new PropertyModel<String>(this, "key")));
			tf.setRequired(true);
			tf.setOutputMarkupId(true);
			
			final DropDownChoice<String> keySuggestions;
			add(keySuggestions = new DropDownChoice<String>("keySuggestions", new Model<String>(), choicesModel) {
				@Override
				public boolean isVisible()
				{
					return VariablesPanel.this.getModelObject() instanceof GlobalContainerNode == false;
				}
			});
			keySuggestions.setNullValid(true);
			
			keySuggestions.add(new AjaxFormComponentUpdatingBehavior("onchange")
			{
				@Override
				protected void onUpdate(AjaxRequestTarget target)
				{
					tf.setModelObject(keySuggestions.getModelObject());
					keySuggestions.setModelObject(null);
					target.addComponent(tf);
					target.addComponent(keySuggestions);
					target.focusComponent(tf);
				}
			});

			add(new TextField<String>("value", new PropertyModel<String>(this, "value")).setRequired(true));

			add(new AjaxButton("submit")
			{
				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form)
				{					
					AbstractContainer node = (AbstractContainer) VariablesPanel.this.getModelObject();
					node.setVariableValue(key, value);
					node.save();
					onItemAdded();
					key = null;
					value = null;
					target.addComponent(form);
					target.addChildren(findParent(VariablesPanel.class), FeedbackPanel.class);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form)
				{
					target.addChildren(findParent(VariablesPanel.class), FeedbackPanel.class);
				}
			});
			
			tf.add(new IValidator()
			{
				public void validate(IValidatable validatable)
				{
					String key = (String) validatable.getValue();
					
					AbstractContainer node = (AbstractContainer) VariablesPanel.this.getModelObject();
					
					if (key.contains("/") || key.contains(":"))
					{
						report(validatable, "keyValidator.invalidKey", key);
					}
					else if (node.getVariableValue(key, false) != null)
					{
						report(validatable, "keyValidator.duplicateKey", key);
					}
				}
				private void report(IValidatable validatable, String messageKey, String key)
				{					
					validatable.error(new ValidationError().addMessageKey(messageKey).setVariable("key", key));
				}
			});
		}

		private String key;
		private String value;

		abstract protected void onItemAdded();
	};

}
