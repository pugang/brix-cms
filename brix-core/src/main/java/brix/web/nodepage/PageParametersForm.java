package brix.web.nodepage;

import org.apache.wicket.Component;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * Stateless form that allows {@link PageParametersAware} components to
 * contribute state to URL after form submission.
 * 
 * @author Matej Knopp
 * 
 * @param <T>
 */
public class PageParametersForm<T> extends StatelessForm<T>
{

	public PageParametersForm(String id)
	{
		super(id);
	}

	public PageParametersForm(String id, IModel<T> model)
	{
		super(id, model);
	}

	@Override
	protected String getInputNamePrefix()
	{
		return "brix:";
	}

	@Override
	protected void onSubmit()
	{
		super.onSubmit();
		getRequestCycle().setRequestTarget(getRequestTarget());
	}

	protected IRequestTarget getRequestTarget()
	{
		final BrixPageParameters parameters = new BrixPageParameters(getInitialParameters());
		getPage().visitChildren(PageParametersAware.class, new IVisitor<Component>()
		{
			public Object component(Component component)
			{
				((PageParametersAware) component).contributeToPageParameters(parameters);
				return IVisitor.CONTINUE_TRAVERSAL;
			}
		});
		contributeToPageParameters(parameters);
		IRequestTarget target = new BrixNodeRequestTarget((BrixNodeWebPage) getPage(), parameters);
		return target;
	}

	@Override
	protected void writeParamsAsHiddenFields(String[] params, AppendingStringBuffer buffer)
	{
		for (int j = 0; j < params.length; j++)
		{
			String[] pair = params[j].split("=");

			if (pair[0].startsWith(getInputNamePrefix()))
			{
				buffer.append("<input type=\"hidden\" name=\"").append(pair[0]).append("\" value=\"").append(
						pair.length > 1 ? pair[1] : "").append("\" />");
			}
		}
	}

	@Override
	protected boolean encodeUrlInHiddenFields()
	{
		return true;
	}

	@Override
	protected void onComponentTag(ComponentTag tag)
	{
		super.onComponentTag(tag);
		BrixNodeWebPage page = (BrixNodeWebPage) getPage();
		BrixPageParameters parameters = new BrixPageParameters(page.getBrixPageParameters());
		for (String s : parameters.getQueryParamKeys())
		{
			if (s.startsWith("brix:") || s.equals("0"))
			{
				parameters.removeQueryParam(s);
			}
		}
		tag.put("action", RequestCycle.get().urlFor(new BrixNodeRequestTarget(page, parameters)));
	}

	/**
	 * Allows to change {@link BrixPageParameters} after all other components
	 * have contributed their state to it. This method can be used to
	 * postprocess the URL after form submission.
	 */
	// Note, it is intentional that this class doesn't implement
	// PageParametersAware. This method needs to be invoked after all other
	// components have contributed their state. Also it should be only used
	// when this Form is constructing the URL.
	protected void contributeToPageParameters(BrixPageParameters parameters)
	{

	}

	/**
	 * Returns the initial {@link BrixPageParameters} used to build the URL
	 * after form submission.
	 * 
	 * @return
	 */
	protected BrixPageParameters getInitialParameters()
	{		
		return new BrixPageParameters();
	}

}
