package org.molgenis.framework.ui;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.framework.MolgenisService;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.security.Login;
import org.molgenis.framework.ui.ScreenModel.Show;
import org.molgenis.framework.ui.html.FreemarkerInput;
import org.molgenis.framework.ui.html.HtmlSettings;
import org.molgenis.framework.ui.html.RichtextInput;
import org.molgenis.framework.ui.html.render.RenderDecorator;
import org.molgenis.util.EmailService;
import org.molgenis.util.FileLink;
import org.molgenis.util.HttpServletRequestTuple;
import org.molgenis.util.HandleRequestDelegationException;
import org.molgenis.util.Tuple;

/**
 * The root screen for any MOLGENIS application.
 * <p>
 * The UserInterface manages a Tree of ScreenControllers. A UserInterface is
 * backed by exactly one database (for persistent data) and one Login object
 * (taking care of authentication/authorization).
 */
public class ApplicationController extends
		SimpleScreenController<ApplicationModel>
{
	public static String MOLGENIS_TEMP_PATH = "molgenis_temp";
	/** autogenerated */
	private static final long serialVersionUID = 3108474555679524568L;
	/** */
	private static final transient Logger logger = Logger
			.getLogger(ApplicationController.class.getSimpleName());
	/** The login * */
	private Login login;
	/** The email service used */
	private EmailService emailService;
	/** Other services, mapped by path */
	private Map<String,MolgenisService> services;
	/** The current base url that you may need in your apps */
	private String baseUrl;
	/** Galaxy url*/
	private String galaxyUrl;
	/** Molgenis options from generted*/
	private MolgenisOptions options;

	/**
	 * Construct a user interface for a database.
	 * 
	 * @param login
	 *            for authentication/authorization
	 */
	public ApplicationController(MolgenisOptions options, Login login)
	{
		super("molgenis_userinterface_root", null, null); // this is the root of
															// the screen tree.
		this.setModel(new ApplicationModel(this));
		this
				.setView(new FreemarkerView("ApplicationView.ftl", this
						.getModel()));

		// this.database = db;
		this.setLogin(login);
		this.setOptions(options);
		
		//set default render decorators
		try
		{
			HtmlSettings.defaultRenderDecorator =(RenderDecorator) Class.forName(options.render_decorator).newInstance();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ApplicationController(MolgenisOptions options, Login login, EmailService email)
	{
		this(options, login);
		this.setLogin(login);
		this.setEmailService(email);
	}

	/**
	 * Retrieve the current login
	 * 
	 * @return Login
	 */
	public Login getLogin()
	{
		return login;
	}

	/**
	 * Set the Login object that is used for authentication.
	 * 
	 * @param login
	 */
	public void setLogin(Login login)
	{
		this.login = login;
	}

	/**
	 * Retrieve the database that is used by this user interface.
	 * 
	 * @return db the database
	 */
	// public Database getDatabase()
	// {
	// return database;
	// }

	// public void setDatabase(Database database)
	// {
	// logger.info("replacing database "+this.database+" with "+database);
	// this.database = database;
	// }

	@Override
	public FileLink getTempFile() throws IOException
	{
		// File temp = new File("d:\\Temp\\"+System.currentTimeMillis());
		// String tempDir = System.getProperty("java.io.tmpdir");
		File temp = File.createTempFile(MOLGENIS_TEMP_PATH, "");
		logger.debug("create temp file: " + temp);
		return new FileLink(temp, "download/" + temp.getName());
	}

	/**
	 * Convenience method that delegates an event to the controller of the
	 * targetted screen.
	 * 
	 * @param db
	 *            reference to the database
	 * @param request
	 *            with the event
	 */
	public void handleRequest(Database db, Tuple request) throws Exception, HandleRequestDelegationException
	{
		this.handleRequest(db, request, null);
	}

	/**
	 * Convenience method that delegates the refresh to its ScreenController.
	 */
	public void reload(Database db)
	{
		for (ScreenController<?> s : this.getChildren())
		{
			try
			{
				s.reload(db);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				s.getModel().getMessages().add(
						new ScreenMessage("reload failed: " + e.getMessage(),
								false));
			}
		}

		// if(this.getController() != this)
		// {
		// this.getController().reload(db);
		// }
		// else
		// {
		// //refresh whole selected subtree
		// if(this.getSelected() != null)
		// this.getSelected().getController().reload(db);
		// }
	}

	// removed to cleanup MVC pattern where controller manages all
	// @Override
	// public Templateable getScreen()
	// {
	// // TODO Auto-generated method stub
	// return this;
	// }
	//	
	// @Override
	// public String getViewName()
	// {
	// return this.getClass().getSimpleName();
	// }

	@Override
	public Show handleRequest(Database db, Tuple request, OutputStream out) throws HandleRequestDelegationException, Exception
	{
		logger.info("delegating handleRequest(" + request.toString() + ")");
		String screen = request.getString(ScreenModel.INPUT_TARGET);

		// action for me?
		if (screen != null && screen.equals(this.getName()))
		{
			if (request.getString("select") != null)
			{
				// the screen to select
				ScreenController<?> selected = this.get(request
						.getString("select"));

				// now we must make sure that alle menu's above select 'me'
				ScreenController<?> currentParent = selected.getParent();
				ScreenController<?> currentChild = selected;
				while (currentParent != null)
				{
					if (currentParent instanceof MenuController)
					{
						((MenuController) currentParent)
								.setSelected(currentChild.getName());
					}
					currentChild = currentParent;
					currentParent = currentParent.getParent();
				}
			}
			return Show.SHOW_MAIN;
		}
		// No target set -> handle centrally.
		else
		{
			if(!request.isNull("GALAXY_URL"))
			{
				//
				// If a user navigated to Molgenis from a session on a Galaxy server, 
				// we keep track of the Galaxy server address, so when this user wants 
				// to send (upload) data to Galaxy we know which one to send the data to.
				// (In fact we do not send the data directly, but an URL that can be used 
				// by Galaxy to fetch the data)
				//
				this.setGalaxyUrl(request.getString("GALAXY_URL"));
				logger.info("User was forwarded to Molgenis running @ "+this.getApplicationUrl());
				logger.info("User was forwarded to Molgenis by Galaxy running @ "+this.getGalaxyUrl());
			}
		}

		// Delegate
		ScreenController<?> target = get(screen);
		if (target != null)
		{
			if (!target.equals(this)) return target.handleRequest(db, request, null);
		}
		else
			logger.debug("handleRequest(" + request
					+ "): no request needs to be handled");
		
		return Show.SHOW_MAIN;
	}

	public EmailService getEmailService()
	{
		return emailService;
	}

	public void setEmailService(EmailService emailService)
	{
		this.emailService = emailService;
	}

	// @Override
	// public boolean isVisible()
	// {
	// // TODO Auto-generated method stub
	// return true;
	// }
	//
	// @Override
	// public String getViewTemplate()
	// {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void reset()
	// {
	// // TODO Auto-generated method stub
	//		
	// }

	public void clearAllMessages()
	{
		for (ScreenController<?> s : this.getAllChildren())
		{
			s.getModel().getMessages().clear();
		}
	}

	/**
	 * Get the database object for this application
	 * 
	 * @return
	 * @throws Exception
	 */
	public Database getDatabase()
	{
		throw new UnsupportedOperationException(
				"getDatabase must be implemented for use");
	}

	/**
	 * Add a MolgenisService. TODO: we would like to refactor this to also take
	 * cxf annotated services
	 * @throws Exception 
	 */
	public void addService(MolgenisService matrixView) throws NameNotUniqueException
	{
		if(this.services.containsKey(matrixView.getName())) throw new NameNotUniqueException("addService failed: path already exists");
		this.services.put(matrixView.getName(), matrixView);
	}

	/**
	 * The base url of this app. Generally the path up to %/molgenis.do
	 * @return
	 */
	public String getApplicationUrl()
	{
		return baseUrl;
	}

	/**
	 * This method is used only internally.
	 * 
	 * @param baseUrl
	 */
	public void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public String getGalaxyUrl()
	{
		return galaxyUrl;
	}

	public void setGalaxyUrl(String galaxyUrl)
	{
		this.galaxyUrl = galaxyUrl;
	}
	
	public String getCustomHtmlHeaders()
	{
		//TODO: this should be made more generic
		return 
		new FreemarkerInput("dummy").getCustomHtmlHeaders()+
		new RichtextInput("dummy").getCustomHtmlHeaders() +
		super.getCustomHtmlHeaders();
	}

	public MolgenisOptions getOptions()
	{
		return options;
	}

	public void setOptions(MolgenisOptions options)
	{
		this.options = options;
	}
}
