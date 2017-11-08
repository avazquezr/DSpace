/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.submit.step;

import org.atteo.evo.inflector.English;
import org.dspace.app.rest.model.BitstreamRest;
import org.dspace.app.rest.model.step.DataLicense;
import org.dspace.app.rest.submit.AbstractRestProcessingStep;
import org.dspace.app.util.SubmissionStepConfig;
import org.dspace.content.Bitstream;
import org.dspace.content.WorkspaceItem;
import org.dspace.core.Constants;

public class LicenseStep extends org.dspace.submit.step.LicenseStep implements AbstractRestProcessingStep {

	private static final String DC_RIGHTS_DATE = "dc.rights.date";

	@Override
	public DataLicense getData(WorkspaceItem obj, SubmissionStepConfig config) throws Exception {
		DataLicense result = new DataLicense();
		Bitstream bitstream = bitstreamService.getBitstreamByName(obj.getItem(), Constants.LICENSE_BUNDLE_NAME, Constants.LICENSE_BITSTREAM_NAME);
		if(bitstream!=null) {
			String acceptanceDate = bitstreamService.getMetadata(bitstream, DC_RIGHTS_DATE);
			result.setAcceptanceDate(acceptanceDate);
			result.setUrl(configurationService.getProperty("dspace.url")+"/api/"+BitstreamRest.CATEGORY +"/"+ English.plural(BitstreamRest.NAME) + "/" + bitstream.getID() + "/content");
		}
		return result;
	}

}