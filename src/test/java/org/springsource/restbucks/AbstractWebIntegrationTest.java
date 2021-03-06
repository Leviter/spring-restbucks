/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.restbucks;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.ShallowEtagHeaderFilter;
import org.springsource.restbucks.Restbucks.WebConfiguration;

/**
 * Base class to derive concrete web test classes from.
 * 
 * @author Oliver Gierke
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfiguration.class)
public abstract class AbstractWebIntegrationTest {

	@Autowired protected WebApplicationContext context;

	@Autowired protected LinkDiscoverer links;
	protected MockMvc mvc;

	@Before
	public void setUp() {

		OpenEntityManagerInViewFilter oemivFilter = new OpenEntityManagerInViewFilter();
		oemivFilter.setServletContext(context.getServletContext());

		mvc = MockMvcBuilders.webAppContextSetup(context). //
				addFilter(new ShallowEtagHeaderFilter()). //
				addFilter(oemivFilter). //
				build();
	}

	/**
	 * Creates a {@link ResultMatcher} that checks for the presence of a link with the given rel.
	 * 
	 * @param rel
	 * @return
	 */
	protected ResultMatcher linkWithRelIsPresent(final String rel) {
		return new LinkWithRelMatcher(rel, true);
	}

	/**
	 * Creates a {@link ResultMatcher} that checks for the non-presence of a link with the given rel.
	 * 
	 * @param rel
	 * @return
	 */
	protected ResultMatcher linkWithRelIsNotPresent(String rel) {
		return new LinkWithRelMatcher(rel, false);
	}

	private class LinkWithRelMatcher implements ResultMatcher {

		private final String rel;
		private final boolean present;

		public LinkWithRelMatcher(String rel, boolean present) {
			this.rel = rel;
			this.present = present;
		}

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.test.web.servlet.ResultMatcher#match(org.springframework.test.web.servlet.MvcResult)
		 */
		@Override
		public void match(MvcResult result) throws Exception {

			String content = result.getResponse().getContentAsString();
			assertThat(links.findLinkWithRel(rel, content), is(present ? notNullValue() : nullValue()));
		}
	}
}
