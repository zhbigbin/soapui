/*
 *  soapUI, copyright (C) 2004-2012 smartbear.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.impl.rest.actions.resource;

import com.eviware.soapui.config.RestParametersConfig;
import com.eviware.soapui.impl.rest.RestMethod;
import com.eviware.soapui.impl.rest.RestRequest;
import com.eviware.soapui.impl.rest.RestRequestInterface;
import com.eviware.soapui.impl.rest.RestResource;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTable;
import com.eviware.soapui.impl.rest.panels.resource.RestParamsTableModel;
import com.eviware.soapui.impl.rest.support.RestParamProperty;
import com.eviware.soapui.impl.rest.support.RestParamsPropertyHolder;
import com.eviware.soapui.impl.rest.support.XmlBeansRestParamsTestPropertyHolder;
import com.eviware.soapui.impl.wsdl.support.HelpUrls;
import com.eviware.soapui.model.testsuite.TestProperty;
import com.eviware.soapui.support.MessageSupport;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;

import static com.eviware.soapui.impl.rest.actions.support.NewRestResourceActionBase.ParamLocation;


/**
 * Actions for importing an existing soapUI project file into the current
 * workspace
 *
 * @author Ole.Matzura
 */

public class NewRestMethodAction extends AbstractSoapUIAction<RestResource>
{
	public static final String SOAPUI_ACTION_ID = "NewRestMethodAction";
	public static final MessageSupport messages = MessageSupport.getMessages( NewRestMethodAction.class );
	private XFormDialog dialog;

	public NewRestMethodAction()
	{
		super( messages.get( "title" ), messages.get( "description" ) );
	}

	public void perform( RestResource resource, Object param )
	{
		if( dialog == null )
		{
			dialog = ADialogBuilder.buildDialog( Form.class );
			dialog.setBooleanValue( Form.CREATEREQUEST, true );
		}

		dialog.setValue( Form.RESOURCENAME, "Method " + ( resource.getRestMethodCount() + 1 ) );

		XmlBeansRestParamsTestPropertyHolder params;
		if( param instanceof XmlBeansRestParamsTestPropertyHolder )
			params = ( XmlBeansRestParamsTestPropertyHolder )param;
		else
			params = new XmlBeansRestParamsTestPropertyHolder( null, RestParametersConfig.Factory.newInstance() );


		RestParamsTableModel model = new RestParamsTableModel( params );
		RestParamsTable paramsTable = new RestParamsTable( params, false, model, ParamLocation.METHOD, true, false );

		dialog.getFormField( Form.PARAMSTABLE ).setProperty( "component", paramsTable );

		if( dialog.show() )
		{
			RestMethod method = resource.addNewMethod( dialog.getValue( Form.RESOURCENAME ) );
			method.setMethod( RestRequestInterface.RequestMethod.valueOf( dialog.getValue( Form.METHOD ) ) );
			paramsTable.extractParams( method.getParams(), ParamLocation.METHOD );

			UISupport.select( method );

			if( dialog.getBooleanValue( Form.CREATEREQUEST ) )
			{
				createRequest( method, method.getParams() );
			}
		}
	}

	protected void createRequest( RestMethod method, RestParamsPropertyHolder params )
	{
		RestRequest request = method.addNewRequest( "Request " + ( method.getRequestCount() + 1 ) );
		for ( TestProperty param : params.getProperties().values())
		{
			( ( RestParamProperty )param ).addPropertyChangeListener( request );
		}

		UISupport.showDesktopPanel( request );
	}

	@AForm( name = "Form.Title", description = "Form.Description", helpUrl = HelpUrls.NEWRESTSERVICE_HELP_URL, icon = UISupport.TOOL_ICON_PATH )
	public interface Form
	{
		@AField( description = "Form.ResourceName.Description", type = AFieldType.STRING )
		public final static String RESOURCENAME = messages.get( "Form.ResourceName.Label" );

		@AField( description = "Form.Method.Description", type = AFieldType.ENUMERATION, values = { "GET", "POST", "PUT",
				"DELETE", "HEAD", "PATCH" } )
		public final static String METHOD = messages.get( "Form.Method.Label" );

		@AField( description = "Form.ParamsTable.Description", type = AFieldType.COMPONENT )
		public final static String PARAMSTABLE = messages.get( "Form.ParamsTable.Label" );

		@AField( description = "Form.CreateRequest.Description", type = AFieldType.BOOLEAN )
		public final static String CREATEREQUEST = messages.get( "Form.CreateRequest.Label" );
	}
}
