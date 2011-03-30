package org.molgenis.framework.ui.commands;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.ui.FormController;
import org.molgenis.framework.ui.FormModel;
import org.molgenis.framework.ui.ScreenModel;
import org.molgenis.framework.ui.SimpleModel;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.util.Entity;
import org.molgenis.util.Tuple;

/**
 * The command to add a new record
 */
public class AddCommand<E extends Entity> extends SimpleCommand<E>
{
	private static final long serialVersionUID = 1512493344265778285L;

	public AddCommand(String name, SimpleModel<E> parent)
	{
		super(name, parent);
		this.setLabel("Add new record");
		this.setIcon("generated-res/img/new.png");
		this.setDialog(true);
		this.setMenu("Edit");
		this.setToolbar(true);
	}

	@Override
	public List<HtmlInput> getInputs() throws DatabaseException
	{
		// delegate to the formscreen
		return this.getFormScreen().getNewRecordForm().getInputs();
	}

	@Override
	public List<HtmlInput> getActions()
	{
		List<HtmlInput> inputs = new ArrayList<HtmlInput>();

//		HiddenInput inDialog = new HiddenInput("__indialog","add");
//		inputs.add(inDialog);
		
		ActionInput submit = new ActionInput("add", ActionInput.Type.SAVE);
		submit.setIcon("generated-res/img/save.png");
		inputs.add(submit);

		ActionInput cancel = new ActionInput("", ActionInput.Type.CLOSE);
		cancel.setIcon("generated-res/img/cancel.png");
		inputs.add(cancel);

		return inputs;
	}
	
	@Override
	public boolean isVisible()
	{
		//hide add button if the screen is readonly
		return !this.getFormScreen().isReadonly(); 
	}

	@Override
	public ScreenModel.Show handleRequest(Database db, Tuple request, PrintWriter downloadStream) throws ParseException,
			DatabaseException, IOException
	{		
		if (request.getString(FormModel.INPUT_SHOW) == null)
		{
			// delegate to the form controller
			((FormController) this.getScreen().getController()).doAdd(db, request);
		}
		return ScreenModel.Show.SHOW_MAIN;
	}
}