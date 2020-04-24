/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.dspace.app.cris.model.ACrisObject;
import org.dspace.app.cris.model.jdyna.ACrisNestedObject;
import org.dspace.app.cris.model.jdyna.DynamicObjectType;
import org.dspace.app.cris.model.jdyna.DynamicPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.OUPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.ProjectPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.RPPropertiesDefinition;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.util.DCInput;
import org.dspace.app.util.DCInputSet;
import org.dspace.app.util.DCInputsReader;
import org.dspace.app.util.DCInputsReaderException;
import org.dspace.content.DSpaceObject;
import org.dspace.content.IMetadataValue;
import org.dspace.content.Item;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.core.I18nUtil;
import org.dspace.discovery.DiscoverQuery;
import org.dspace.discovery.SolrServiceImpl;
import org.dspace.discovery.SolrServiceIndexPlugin;
import org.dspace.discovery.SolrServiceSearchPlugin;
import org.dspace.discovery.configuration.DiscoverySearchFilter;
import org.dspace.services.ConfigurationService;

import it.cilea.osd.jdyna.model.ANestedPropertiesDefinition;
import it.cilea.osd.jdyna.model.ANestedProperty;
import it.cilea.osd.jdyna.model.ATypeNestedObject;
import it.cilea.osd.jdyna.model.PropertiesDefinition;
import it.cilea.osd.jdyna.model.Property;
import it.cilea.osd.jdyna.widget.WidgetCheckRadio;

/**
 * 
 * @author Luigi Andrea Pascarelli
 *
 */
public class CrisValuePairsIndexPlugin implements CrisServiceIndexPlugin,
        SolrServiceIndexPlugin, SolrServiceSearchPlugin
{

    private static final Logger log = Logger
            .getLogger(CrisValuePairsIndexPlugin.class);

    private ApplicationService applicationService;

    private ConfigurationService configurationService;

    private Map<String, DCInputsReader> dcInputsReader = new HashMap<>();

    private String separator;

    private void init() throws DCInputsReaderException
    {
        if (separator == null)
        {
            separator = configurationService
                    .getProperty("discovery.solr.facets.split.char");
            if (separator == null)
            {
                separator = SolrServiceImpl.FILTER_SEPARATOR;
            }
        }
        
        if(dcInputsReader.isEmpty()) {
            for (Locale locale : I18nUtil.getSupportedLocales())
            {
                dcInputsReader.put(locale.getLanguage(),
                    new DCInputsReader(I18nUtil.getInputFormsFileName(locale)));
            }
        }

    }

    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACrisObject<P, TP, NP, NTP, ACNO, ATNO> crisObject,
            SolrInputDocument document,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
    	try
    	{
    		init();
    	}
    	catch (DCInputsReaderException e)
    	{
    		log.error(e.getMessage(), e);
    	}
    	if (crisObject != null)
    	{
    		String language = I18nUtil.getDefaultLocale().getLanguage();
    		String schema = "cris" + crisObject.getPublicPath();
    		List<TP> allPropertiesDefinition = applicationService
    				.getAllPropertiesDefinitionWithRadioCheckDropdown(
    						crisObject.getClassPropertiesDefinition());
    		for (TP pd : allPropertiesDefinition)
    		{
    			List<P> storedP = crisObject.getAnagrafica4view()
    					.get(pd.getShortName());
    			for (P stored_value : storedP)
    			{
    				String field = schema + "."
    						+ stored_value.getTypo().getShortName();
    				String displayVal = getCheckRadioDisplayValue(
    						(((WidgetCheckRadio) pd.getRendering())
    								.getStaticValues()),
    						stored_value.toString());
    				if (StringUtils.isBlank(displayVal))
    				{
    					displayVal = stored_value.toString();
    				}
    				String prefixedDisplayVal = language + "_" + displayVal;

    				document.removeField(field);
    				document.addField(field, stored_value.getValue().toString());
//    				document.addField(field + "_multilanguage" , language + "_" + displayVal);
    				
    				buildSearchFilter(document, searchFilters,
    						stored_value.toString(), field, field,
    						displayVal, prefixedDisplayVal, null, null, true);

    			}
    		}
    	}
    }

    @Override
    public <P extends Property<TP>, TP extends PropertiesDefinition, NP extends ANestedProperty<NTP>, NTP extends ANestedPropertiesDefinition, ACNO extends ACrisNestedObject<NP, NTP, P, TP>, ATNO extends ATypeNestedObject<NTP>> void additionalIndex(
            ACNO crisObject, SolrInputDocument document,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
//        TODO manage nested
//        try
//        {
//            init();
//        }
//        catch (DCInputsReaderException e)
//        {
//            log.error(e.getMessage(), e);
//        }
//        if (crisObject != null)
//        {
//            ICrisObject<P, TP> parent = (ICrisObject<P, TP>) crisObject
//                    .getParent();
//            String confName = "ncris" + parent.getPublicPath();
//            String schema = confName + crisObject.getTypo().getShortName();
//            List<NTP> allPropertiesDefinition = applicationService
//                    .getAllPropertiesDefinitionWithRadioCheckDropdown(
//                            crisObject.getClassPropertiesDefinition());
//            for (NTP pd : allPropertiesDefinition)
//            {
//                List<NP> storedP = crisObject.getAnagrafica4view()
//                        .get(pd.getShortName());
//                for (NP stored_value : storedP)
//                {
//                    String field = schema + "."
//                            + stored_value.getTypo().getShortName();
//                    String displayVal = getCheckRadioDisplayValue(
//                            (((WidgetCheckRadio) pd.getRendering())
//                                    .getStaticValues()),
//                            stored_value.toString());
//                    document.removeField(field + "_authority");
//                    document.addField(field + "_authority", stored_value);
//                    document.removeField(field);
//                    document.addField(field, displayVal);
//                    buildSearchFilter(document, searchFilters,
//                            stored_value.toString(), field, field, displayVal);
//                }
//            }
//        }
    }

    @Override
    public void additionalIndex(Context context, DSpaceObject dso,
            SolrInputDocument document,
            Map<String, List<DiscoverySearchFilter>> searchFilters)
    {
        try
        {
            init();
        }
        catch (DCInputsReaderException e)
        {
            log.error(e.getMessage(), e);
        }
        if (dso != null)
        {
            if (dso.getType() == Constants.ITEM)
            {
                Item item = (Item) dso;
                try
                {
                    Map<String, List<String>> filterMapToWrite = new HashMap<String, List<String>>();
                    for (String language : dcInputsReader.keySet())
                    {
                    	Boolean isDefaultLanguage = false;
                    	if (StringUtils.equals(language, I18nUtil.getDefaultLocale().getLanguage())) 
                    	{
							isDefaultLanguage = true;
						}
                        DCInputSet dcInputSet = dcInputsReader.get(language)
                                .getInputs(
                                        item.getOwningCollection().getHandle());

                        for (int i = 0; i < dcInputSet.getNumberPages(); i++)
                        {
                            DCInput[] dcInput = dcInputSet.getPageRows(i, false,
                                    false);
                            for (DCInput myInput : dcInput)
                            {
                                if (StringUtils
                                        .isNotBlank(myInput.getPairsType()))
                                {
                                    for (IMetadataValue metadatum : item.getMetadata(
                                            myInput.getSchema(),
                                            myInput.getElement(),
                                            myInput.getQualifier(), Item.ANY))
                                    {
                                        String stored_value = metadatum.getValue();
                                        String displayVal = myInput
                                                .getDisplayString(null,
                                                        stored_value);
                                        String prefixedDisplayVal = language
                                                + "_" + displayVal;
                                        if (StringUtils.isBlank(displayVal))
                                        {
                                            displayVal = stored_value;
                                        }
                                        document.removeField(metadatum.getMetadataField().toString('.'));
                                        document.addField(metadatum.getMetadataField().toString('.'),
	                                            stored_value);
//                                        document.addField(metadatum.getMetadataField().toString('.') + "_multilanguage" , language + "_" + displayVal);
                                        
                                        String unqualifiedField = myInput
                                                .getSchema() + "."
                                                + myInput.getElement() + "."
                                                + Item.ANY;
                                        
                                        String authority = metadatum.getAuthority();
                                        buildSearchFilter(document,
                                                searchFilters,
                                                stored_value.toString(),
                                                metadatum.getMetadataField().toString('.'),
                                                unqualifiedField, displayVal,
                                                prefixedDisplayVal, filterMapToWrite, authority, isDefaultLanguage);
                                        
                                    }
                                }
                            }
                        }
                    }
                    for(String indexFieldName : filterMapToWrite.keySet()) 
                    {
                    	for (String value : filterMapToWrite.get(indexFieldName)) 
                    	{							
                    		document.addField(indexFieldName, value);
						}
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }

            }
        }
    }

    @Override
    public void additionalSearchParameters(Context context,
            DiscoverQuery discoveryQuery, SolrQuery solrQuery)
    {
        String language = null;
        try
        {
        	if(context != null && context.getCurrentLocale()!=null) {
        		language = context.getCurrentLocale().getLanguage();
        	}
            if(StringUtils.isBlank(language)) {
                language = configurationService.getProperty("default.locale");
            }
            init();            
        }
        catch (Exception e)
        {
            log.warn(e.getMessage(), e);
        }
        if (StringUtils.isNotBlank(language))
        {
            Set<String> result = new HashSet<String>();
            Iterator<String> iterator = dcInputsReader.get(language)
                    .getPairsNameIterator();
            while (iterator.hasNext())
            {
                result.add("valuepairsname_" + iterator.next());
            }

            Set<String> pds = additionalSearchParameter(
                    RPPropertiesDefinition.class);
            for (String pd : pds)
            {
                result.add("crisrp." + pd);
            }
            pds = additionalSearchParameter(ProjectPropertiesDefinition.class);
            for (String pd : pds)
            {
                result.add("crisproject." + pd);
            }
            pds = additionalSearchParameter(OUPropertiesDefinition.class);
            for (String pd : pds)
            {
                result.add("crisou." + pd);
            }
            pds = additionalSearchParameter(DynamicPropertiesDefinition.class);
            for (String pd : pds)
            {
                List<DynamicObjectType> dyn = applicationService
                        .getList(DynamicObjectType.class);
                for (DynamicObjectType dy : dyn)
                {
                    if (pd.startsWith(dy.getShortName()))
                    {
                        result.add("cris" + dy.getShortName() + "." + pd);
                    }
                }
            }
            // TODO manage nested
            // result.addAll(additionalSearchParameter(RPNestedPropertiesDefinition.class));
            // result.addAll(additionalSearchParameter(ProjectNestedPropertiesDefinition.class));
            // result.addAll(additionalSearchParameter(OUNestedPropertiesDefinition.class));
            // result.addAll(additionalSearchParameter(DynamicNestedPropertiesDefinition.class));

            for (String rr : result)
            {
                solrQuery.addField(rr);
            }
        }

    }

    private <TP extends PropertiesDefinition> Set<String> additionalSearchParameter(
            Class<TP> clazz)
    {
        Set<String> result = new HashSet<String>();
        List<TP> allPropertiesDefinitionRP = applicationService
                .getAllPropertiesDefinitionWithRadioCheckDropdown(clazz);
        for (TP pds : allPropertiesDefinitionRP)
        {
            result.add(pds.getShortName());
        }
        return result;
    }

    public ApplicationService getApplicationService()
    {
        return applicationService;
    }

    public void setApplicationService(ApplicationService applicationService)
    {
        this.applicationService = applicationService;
    }

    public static String getCheckRadioDisplayValue(String staticValues,
            String identifierValue)
    {
        String[] resultTmp = staticValues.split("\\|\\|\\|");
        for (String rr : resultTmp)
        {
            String displayValue = rr;
            String identifyingValue = rr;
            if (rr.contains("###"))
            {
                identifyingValue = rr.split("###")[0];
                displayValue = rr.split("###")[1];
            }
            if (identifyingValue.equals(identifierValue))
            {
                return displayValue;
            }
        }
        return null;
    }

    private void buildSearchFilter(SolrInputDocument document,
            Map<String, List<DiscoverySearchFilter>> searchFilters,
            String stored_value, String field, String unqualifiedField,
            String displayVal, String prefixedDisplayVal, Map<String, List<String>> filterMapToWrite, String authority, Boolean isDefaultLanguage)
    {
        if (searchFilters.containsKey(field))
        {
            List<DiscoverySearchFilter> searchFilterConfigs = searchFilters
                    .get(field);
            if (searchFilterConfigs == null)
            {
                searchFilterConfigs = searchFilters
                        .get(unqualifiedField + "." + Item.ANY);
            }
            
            for (DiscoverySearchFilter searchFilter : searchFilterConfigs)
            {
            	
            	if (isDefaultLanguage) 
            	{
            		document.removeField(searchFilter.getIndexFieldName());
            		
            		addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName(), displayVal);
            		addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_ac", 
            				displayVal.toLowerCase() 
            					+ separator 
            					+ displayVal);
            		addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_keyword", 
            				displayVal);
            		addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_acid", 
            				displayVal.toLowerCase() 
            					+ separator 
            					+ displayVal);
            		addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_filter", 
            				displayVal.toLowerCase() 
            					+ separator 
            					+ displayVal);
				}
            	
            	addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_ac", 
            			prefixedDisplayVal.toLowerCase() + separator 
            				+ displayVal);
            	
            	document.removeField(searchFilter.getIndexFieldName() + "_ac");
            	document.removeField(searchFilter.getIndexFieldName() + "_keyword");
            	document.removeField(searchFilter.getIndexFieldName() + "_acid");
            	document.removeField(searchFilter.getIndexFieldName() + "_filter");
            	if (StringUtils.isNotBlank(authority)) 
	            {
            		addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_keyword",
	                        prefixedDisplayVal + SolrServiceImpl.AUTHORITY_SEPARATOR
	                                + authority);
            		addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_acid",
	                        prefixedDisplayVal.toLowerCase() + separator
	                                + displayVal
	                                + SolrServiceImpl.AUTHORITY_SEPARATOR
	                                + authority);
            		addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_filter",
	                        prefixedDisplayVal.toLowerCase() + separator + displayVal
	                                + SolrServiceImpl.AUTHORITY_SEPARATOR
	                                + authority);
	                document.removeField(searchFilter.getIndexFieldName() + "_authority");
	                addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_authority", authority);
	            }else {
	            	addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_keyword",
	                        prefixedDisplayVal);
	            	addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_acid",
	                        prefixedDisplayVal.toLowerCase() + separator
	                                + displayVal);
	            	addToFilterMap(filterMapToWrite, searchFilter.getIndexFieldName() + "_filter",
	                        prefixedDisplayVal.toLowerCase() + separator + displayVal);
				}
            }
        }
    }
    
    private void addToFilterMap(Map<String, List<String>> filterMapToWrite, String key, String value) 
    {
    	if (filterMapToWrite.containsKey(key))
    	{					
    		filterMapToWrite.get(key).add(value);
		}else 
		{
			List<String> listToPut = new ArrayList<String>();
			
			listToPut.add(value);
			filterMapToWrite.put(key, listToPut);
		}
	}

    public ConfigurationService getConfigurationService()
    {
        return configurationService;
    }

    public void setConfigurationService(
            ConfigurationService configurationService)
    {
        this.configurationService = configurationService;
    }

    public String getSeparator()
    {
        return separator;
    }

    public void setSeparator(String separator)
    {
        this.separator = separator;
    }
    
}