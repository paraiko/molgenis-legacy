/**
 * 
 */
package org.molgenis.framework.ui.commands;

import java.io.PrintWriter;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.FormModel.Mode;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.util.Entity;
import org.molgenis.util.Tuple;

/**
 * This command changes the limit of the number of items shown in listview 
 *
 * @param <E>
 */
public class ChangeListLimitCommand<E extends Entity> extends SimpleCommand<E>
{
	private static final long serialVersionUID = -8328256342346578115L;
	//the limit it should change too
	private int limit = 5;

	public ChangeListLimitCommand(String name, FormModel<E> parentScreen)
	{
		super(name, parentScreen);
		this.setLabel("Show %s items");
		this.setIcon("generated-res/img/limit.png");
		this.setMenu("View");
	}

	@Override
	public boolean isVisible()
	{
		// only show in list view
		return getFormScreen().getMode().equals(Mode.LIST_VIEW);
	}
	
	@Override
	public String getLabel()
	{
		return String.format(super.getLabel(),getLimit());
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, Tuple request, PrintWriter downloadStream)
	{

		getFormScreen().setLimit(getLimit());
		return ScreenModel.Show.SHOW_MAIN;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	@Override
	public List<HtmlInput> getActions()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<HtmlInput> getInputs() throws DatabaseException
	{
		// TODO Auto-generated method stub
		return null;
	}
}