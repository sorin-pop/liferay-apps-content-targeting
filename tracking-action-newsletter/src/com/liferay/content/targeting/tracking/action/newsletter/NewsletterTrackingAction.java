/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.content.targeting.tracking.action.newsletter;

import com.liferay.content.targeting.InvalidTrackingActionException;
import com.liferay.content.targeting.api.model.BaseTrackingAction;
import com.liferay.content.targeting.api.model.TrackingAction;
import com.liferay.content.targeting.model.TrackingActionInstance;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.ListUtil;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.theme.ThemeDisplay;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

/**
 * @author Brian Chan
 */
@Component(immediate = true, service = TrackingAction.class)
public class NewsletterTrackingAction
	extends BaseTrackingAction {

	@Activate
	@Override
	public void activate() {
		super.activate();
	}

	@Deactivate
	@Override
	public void deActivate() {
		super.deActivate();
	}

	@Override
	public List<String> getEventTypes() {
		return ListUtil.fromArray(_EVENT_TYPES);
		}

	@Override
	public String getIcon() {
		return "icon-envelope-alt";
	}

	@Override
	public String getSummary(
		TrackingActionInstance trackingActionInstance, Locale locale) {

		return LanguageUtil.get(
			locale, trackingActionInstance.getTypeSettings());
	}

	@Override
	public String processTrackingAction(
			PortletRequest request, PortletResponse response, String id,
			Map<String, String> values)
		throws InvalidTrackingActionException {

		return values.get("url");
	}

	@Override
	protected void populateContext(
		TrackingActionInstance trackingActionInstance,
		Map<String, Object> context, Map<String, String> values) {

		String alias = StringPool.BLANK;
		String newsletterId = StringPool.BLANK;
		String eventType = StringPool.BLANK;
		String url = StringPool.BLANK;

		if (!values.isEmpty()) {
			// Values from Request

			alias = values.get("alias");
			newsletterId = values.get("elementId");
			eventType = values.get("eventType");
			url = values.get("url");
		}
		else if (trackingActionInstance != null) {
			// Values from DB

			alias = trackingActionInstance.getAlias();
			newsletterId = trackingActionInstance.getElementId();
			eventType = trackingActionInstance.getEventType();
			url = trackingActionInstance.getTypeSettings();
		}

		context.put("alias", alias);
		context.put("elementId", newsletterId);
		context.put("eventType", eventType);
		context.put("eventTypes", getEventTypes());
		context.put("url", url);

		ThemeDisplay themeDisplay = (ThemeDisplay)context.get("themeDisplay");

		String trackURL = themeDisplay.getPortalURL() + "/o/tracking-action-newsletter/track";

		trackURL = HttpUtil.addParameter(
			trackURL, "elementId", "elementIdToken");
		String trackImageURL = HttpUtil.addParameter(trackURL, "imageId", "1");
		trackImageURL = HttpUtil.addParameter(trackImageURL, "email", "");

		context.put("trackImageURL", trackImageURL);

		String trackLinkURL = HttpUtil.addParameter(
			trackURL, "redirect", "redirectToken");
		trackLinkURL = HttpUtil.addParameter(trackLinkURL, "email", "");

		context.put("trackLinkURL", trackLinkURL);
	}

	private static final String[] _EVENT_TYPES = {"view", "click"};

}