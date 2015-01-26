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

package com.liferay.content.targeting.rule.custom.field;

import com.liferay.content.targeting.InvalidRuleException;
import com.liferay.content.targeting.anonymous.users.model.AnonymousUser;
import com.liferay.content.targeting.api.model.BaseRule;
import com.liferay.content.targeting.api.model.Rule;
import com.liferay.content.targeting.model.RuleInstance;
import com.liferay.content.targeting.rule.categories.UserAttributesRuleCategory;
import com.liferay.counter.service.CounterLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.json.JSONException;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.Company;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.ResourceActionsUtil;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portal.util.PortalUtil;
import com.liferay.portlet.expando.model.ExpandoBridge;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import com.liferay.portlet.expando.util.ExpandoBridgeFactoryUtil;

import java.io.Serializable;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

/**
 * @author Eudaldo Alonso
 */
@Component(immediate = true, service = Rule.class)
public class CustomFieldRule extends BaseRule {

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
	public void deleteData(RuleInstance ruleInstance)
		throws PortalException, SystemException {

		JSONObject jsonObj = JSONFactoryUtil.createJSONObject(
			ruleInstance.getTypeSettings());

		long classPK = jsonObj.getLong("classPK");

		ExpandoValueLocalServiceUtil.deleteValues(
			User.class.getName(), classPK);
	}

	@Override
	public boolean evaluate(
			HttpServletRequest request, RuleInstance ruleInstance,
			AnonymousUser anonymousUser)
		throws Exception {

		if (anonymousUser.getUserId() != 0) {
			ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
				WebKeys.THEME_DISPLAY);

			JSONObject jsonObj = JSONFactoryUtil.createJSONObject(
				ruleInstance.getTypeSettings());

			String attributeName = jsonObj.getString("attributeName");
			long classPK = jsonObj.getLong("classPK");

			ExpandoBridge expandoBridge =
				ExpandoBridgeFactoryUtil.getExpandoBridge(
					themeDisplay.getCompanyId(), User.class.getName(), classPK);

			Serializable attribute = expandoBridge.getAttribute(attributeName);

			ExpandoBridge userExpandoBridge =
				ExpandoBridgeFactoryUtil.getExpandoBridge(
					themeDisplay.getCompanyId(), User.class.getName(),
					anonymousUser.getUserId());

			Serializable userAttribute = userExpandoBridge.getAttribute(
				attributeName);

			if (attribute.equals(userAttribute)) {
				return true;
			}
		}

		return false;

		/*
		ExpandoValue expandoValue = ExpandoValueLocalServiceUtil.getValue(
			companyId, className, tableName, columnName, classPK);

		expandoBridge.getAttribute()
		*/
	}

	@Override
	public String getIcon() {
		return "icon-puzzle";
	}

	@Override
	public String getRuleCategoryKey() {
		return UserAttributesRuleCategory.KEY;
	}

	@Override
	public String getSummary(RuleInstance ruleInstance, Locale locale) {
		return StringPool.BLANK;
	}

	@Override
	public String processRule(
			PortletRequest request, PortletResponse response, String id,
			Map<String, String> values)
		throws InvalidRuleException {

		long classPK = GetterUtil.getLong(values.get("classPK"));

		ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
			WebKeys.THEME_DISPLAY);

		try {
			if (classPK <= 0) {
				classPK = CounterLocalServiceUtil.increment();
			}

			Map<String, Serializable> expandoBridgeAttributes =
				PortalUtil.getExpandoBridgeAttributes(
					ExpandoBridgeFactoryUtil.getExpandoBridge(
						themeDisplay.getCompanyId(), User.class.getName()),
					request);

			ExpandoBridge expandoBridge =
				ExpandoBridgeFactoryUtil.getExpandoBridge(
					themeDisplay.getCompanyId(), User.class.getName(), classPK);

			expandoBridge.setAttributes(expandoBridgeAttributes);
		}
		catch (Exception e) {
			throw new InvalidRuleException("cannot-save-custom-fields");
		}

		String attributeName = values.get("attributeName");

		JSONObject jsonObj = JSONFactoryUtil.createJSONObject();

		jsonObj.put("attributeName", attributeName);
		jsonObj.put("classPK", classPK);

		return jsonObj.toString();
	}

	@Override
	protected void populateContext(
		RuleInstance ruleInstance, Map<String, Object> context,
		Map<String, String> values) {

		context.put("className", User.class.getName());

		Locale locale = (Locale)context.get("locale");

		context.put(
			"modelResourceName",
			ResourceActionsUtil.getModelResource(locale, User.class.getName()));

		String attributeName = StringPool.BLANK;
		long classPK = 0;

		if (!values.isEmpty()) {
			classPK = GetterUtil.getLong(values.get("classPK"));
		}
		else if (ruleInstance != null) {
			try {
				JSONObject jsonObj = JSONFactoryUtil.createJSONObject(
					ruleInstance.getTypeSettings());

				attributeName = jsonObj.getString("attributeName");
				classPK = jsonObj.getLong("classPK");
			}
			catch (JSONException jse) {
			}
		}

		context.put("attributeName", attributeName);
		context.put("classPK", classPK);

		Company company = (Company)context.get("company");

		ExpandoBridge expandoBridge =
			ExpandoBridgeFactoryUtil.getExpandoBridge(
				company.getCompanyId(), User.class.getName(), classPK);

		List<String> attributeNames = Collections.list(
			expandoBridge.getAttributeNames());

		context.put("attributeNames", attributeNames);
	}

}