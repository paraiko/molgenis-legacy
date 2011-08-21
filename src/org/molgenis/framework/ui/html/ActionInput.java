package org.molgenis.framework.ui.html;

import java.text.ParseException;

import org.molgenis.util.Tuple;

/**
 * The ActionInput defines action buttons. When clicked, it will result in a new
 * request(__action=&lt;name&gt;)
 */
public class ActionInput extends HtmlInput<Object>
{
	public enum Type
	{
		/** save current record */
		SAVE("Save"),
		/** cancel current action (closes popup window, refreshes opener window) */
		CANCEL("Cancel"),
		/** goto next screen */
		NEXT("Next"),
		// close current screen
		CLOSE("Close"),
		/** automatically close current dialogue */
		AUTOCLOSE("Autoclose"),
		/**
		 * Default: a custom action that requires you to set label, tooltip and
		 * javascript yourself
		 */
		CUSTOM("Set Label, Tooltip, and JavaScriptAction yourself");

		private Type(String tag)
		{
			this.tag = tag;
		}

		public final String tag;

		public String toString()
		{
			return tag;
		}
	}

	/** Path to an icon image */
	private String icon;

	/** Type of submit */
	private Type type;

	/** JavaScript action */
	private String JavaScriptAction;

	/** If false, no label and only icon will be shown */
	private boolean showLabel = true;

	// constructor(s)
	/**
	 * Default constructor, type is submit
	 */
	public ActionInput(String name)
	{
		this(name, Type.CUSTOM);
	}

	/**
	 * Constructor that sets action name and label.
	 * 
	 * @param name
	 * @param label
	 */
	public ActionInput(String name, String label)
	{
		this(name, Type.CUSTOM);
		this.setLabel(label);
	}

	/**
	 * Constructor that sets action name, label and button value (text to show
	 * on button).
	 * 
	 * @param name
	 * @param label
	 */
	public ActionInput(String name, String label, String buttonValue)
	{
		this(name, label);
		this.setTooltip(buttonValue);
	}

	/**
	 * Create a new instance of ActionInput.
	 * 
	 * @param name
	 *            name of the input.
	 * @param type
	 *            type of the input. @see Type
	 */
	public ActionInput(String name, Type type)
	{
		super(name, type);
		this.setType(type);
		this.setLabel(type.toString());
		this.setTooltip(type.toString());
	}

	public ActionInput(Type select_target)
	{
		this(select_target.toString());
		this.setLabel(select_target.toString().replace("_", " "));
	}

	public ActionInput(Tuple t) throws HtmlInputException
	{
		super(t);
	}

	public ActionInput()
	{
	}

	// HtmlInput overloads
	@Override
	public String toHtml()
	{
		if (this.uiToolkit == UiToolkit.ORIGINAL)
		{
			return this.renderDefault();

		}
		else if (this.uiToolkit == UiToolkit.DOJO)
		{
			return this.renderDojo();
		}
		else if (this.uiToolkit == UiToolkit.JQUERY)
		{
			return this.renderJquery();
		}
		return "ERROR";
	}

	public String renderDefault()
	{
		StringBuffer input = new StringBuffer("");
		// TODO: apparently this can be disabled.
		if (getIcon() != null)
		{
			input.append("<img class=\"edit_button\" src=\"" + getIcon()
					+ "\" alt=\"" + getLabel() + "\" onclick=\""
					+ getJavaScriptAction() + "\" title=\"" + this.getTooltip()
					+ "\" id=\"" + this.getId() + "\" style=\""
					+ this.getStyle() + "\" " + tabIndex + " />");
		}
		else
		{
			input.append("<input type=\"submit\" onclick=\""
					+ getJavaScriptAction() + "\" title=\"" + this.getTooltip()
					+ "\" id=\"" + this.getId() + "\"" + "value=\""
					+ "\" style=\"" + this.getStyle()
					+ "\" " + tabIndex + " />");
		}

		return input.toString();
	}

	// attribute methods
	/**
	 * Get the icon that is shown on this button.
	 */
	public String getIcon()
	{
		return icon;
	}

	/**
	 * Set the icon that should be shown on this button
	 * 
	 * @param icon
	 *            relative path from WebContent or classpath.
	 */
	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	/**
	 * Javascript action for this button.
	 * 
	 * @return onClick javascript
	 */
	public String getJavaScriptAction()
	{
		if (JavaScriptAction == null)
		{
			if (this.type == Type.SAVE)
			{
				StringBuffer jScript = new StringBuffer();
				if(this.uiToolkit == UiToolkit.ORIGINAL)
					jScript.append("if( validateForm(molgenis_popup,molgenis_required) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis'+new Date().getTime();} document.forms.molgenis_popup.target = window.opener.name; document.forms.molgenis_popup.submit(); window.close();} else return false;");
				else
					jScript.append("if( $(this.form).valid() && validateForm(molgenis_popup,molgenis_required) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis'+new Date().getTime();} document.forms.molgenis_popup.target = window.opener.name; document.forms.molgenis_popup.submit(); window.close();} return false;");
				//jScript.append("alert('click');");
				
				return jScript.toString();
			}
			else if (this.type == Type.NEXT)
			{
				StringBuffer jScript = new StringBuffer();
				jScript.append("if( validateForm(molgenis_popup,molgenis_required) ) { if( window.opener.name == '' ){ window.opener.name = 'molgenis'+Math.random();} document.forms.molgenis_popup.__show.value='popup'; document.forms.molgenis_popup.submit();} else return false;");
				return jScript.toString();
			}
			else if (this.type == Type.CLOSE)
			{
				return "window.close();";
			}
			else if (this.type == Type.CUSTOM)
			{
				return "__action.value = \'" + getName() + "'; return true;";
			}
		}
		return JavaScriptAction;
	}

	/**
	 * Override default javascript 'onClick' action.
	 * 
	 * @param javaScriptAction
	 */
	public void setJavaScriptAction(String javaScriptAction)
	{
		JavaScriptAction = javaScriptAction;
	}

	/**
	 * The Type of this action
	 * 
	 * @return type
	 * @see Type
	 */
	public Type getType()
	{
		return type;
	}

	/** Set the Type of this action, e.g. SAVE. */
	public void setType(Type type)
	{
		this.type = type;
	}

	@Override
	public String getLabel()
	{
		if (super.getValue() != null && super.getLabel() == super.getValue()) return getName();
		return super.getLabel();
	}

	/** Helper method to produce the html for the icon (&lt;img&gt;) */
	public String getIconHtml()
	{
		// TODO Auto-generated method stub
		return "<img src=\"" + this.getIcon() + "\"/>";
	}

	/** Helper method to produce html for the clickable image */
	public String toIconHtml()
	{
		return "<img class=\"edit_button\" src=\"" + getIcon() + "\" title=\""
				+ getLabel() + "\" onClick=\"" + this.getJavaScriptAction()
				+ "\">";
		// <img class="edit_button" src="generated-res/img/recordview.png"
		// title="view record" alt="edit${offset}"
		// onClick="setInput('${screen.name}_form','_self','','${screen.name}','recordview','iframe'); document.forms.${screen.name}_form.__offset.value='${offset}'; document.forms.${screen.name}_form.submit();">${readonly}</label>
	}

	/** Helper method to render this button as clickable link */
	public String toLinkHtml()
	{
		return "<a title=\"" + this.getDescription() + "\" onclick=\""
				+ this.getJavaScriptAction() + "\">" + getLabel() + "</a>";
	}

	@Override
	public String toHtml(Tuple params) throws ParseException,
			HtmlInputException
	{
		return new ActionInput(params).render();
	}

	@Override
	public String getCustomHtmlHeaders()
	{
		if (this.uiToolkit == UiToolkit.DOJO)
		{
			return "<script type=\"text/javascript\">"
					+ "	dojo.require(\"dijit.form.Button\");" + "</script>";
		}
		return "";
	}

	private String renderJquery()
	{
		// String icon = getIcon() != null ? " iconClass=\"dijitEditorIcon "
		// + getIcon() + "\"" : "";
		// String showLabel = showLabel() == false ? " showLabel=\"false\"" :
		// "";
		String icons = "";
		if (getIcon() != null)
		{
			String icon = getIcon().replace("generated-res/img/","").replace(".png","");
			icons += "{ icons: {primary:'" + getIcon() + "'}"
					+ (isShowLabel() ? "}" : ", text: false }");
		}
		String result = "<button id=\"" + this.getId() + "\"" + " onClick=\""
				+ this.getJavaScriptAction() + "\">" + this.getLabel()
				+ "</button>" + "<script>$(\"#" + this.getId()
				+ "\").button(" + icons + ");</script>\n";

		return result;
	}

	private String renderDojo()
	{
		String icon = getIcon() != null ? " iconClass=\"dijitEditorIcon "
				+ getIcon() + "\"" : "";
		String showLabel = showLabel() == false ? " showLabel=\"false\"" : "";
		String result = "<button class=\"claro\" dojoType=\"dijit.form.Button\""
				+ " type=\"submit\""
				+ icon
				+ showLabel
				+ ">"
				+ this.getLabel()
				+ " <script type=\"dojo/method\" event=\"onClick\" args=\"evt\">"
				+ "var __action = dojo.byId(\"#__action\"); alert(__action.value);"
				+ this.getJavaScriptAction() + "</script></button>";

		return result;
	}

	public boolean showLabel()
	{
		return showLabel;
	}

	public boolean isShowLabel()
	{
		return showLabel;
	}

	public void setShowLabel(boolean showLabel)
	{
		this.showLabel = showLabel;
	}

}
