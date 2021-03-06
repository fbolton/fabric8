/**
 *  Copyright 2005-2014 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;

import io.fabric8.api.Container;
import io.fabric8.api.FabricService;
import io.fabric8.api.Profile;
import io.fabric8.api.jcip.ThreadSafe;
import io.fabric8.api.scr.Validatable;
import io.fabric8.api.scr.ValidatingReference;
import io.fabric8.api.scr.ValidationSupport;

import org.jboss.gravia.utils.IllegalStateAssertion;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;

@ThreadSafe
@Component(name = "io.fabric8.profile.urlhandler", label = "Fabric8 Profile URL Handler", immediate = true, metatype = false)
@Service(URLStreamHandlerService.class)
@Properties({
        @Property(name = "url.handler.protocol", value = "profile")
})
public final class ProfileUrlHandler extends AbstractURLStreamHandlerService implements Validatable {

    private static final String SYNTAX = "profile:<resource name>";

    @Reference(referenceInterface = FabricService.class)
    private final ValidatingReference<FabricService> fabricService = new ValidatingReference<FabricService>();

    private final ValidationSupport active = new ValidationSupport();

    @Activate
    void activate() {
        active.setValid();
    }

    @Deactivate
    void deactivate() {
        active.setInvalid();;
    }

    @Override
    public boolean isValid() {
        return active.isValid();
    }

    @Override
    public void assertValid() {
        active.assertValid();
    }

    @Override
    public URLConnection openConnection(URL url) throws IOException {
        assertValid();
        return new Connection(url);
    }

    private class Connection extends URLConnection {

        public Connection(URL url) throws MalformedURLException {
            super(url);
            if (url.getPath() == null || url.getPath().trim().length() == 0) {
                throw new MalformedURLException("Path can not be null or empty. Syntax: " + SYNTAX);
            }
            if ((url.getHost() != null && url.getHost().length() > 0) || url.getPort() != -1) {
                throw new MalformedURLException("Unsupported host/port in profile url");
            }
            if (url.getQuery() != null && url.getQuery().length() > 0) {
                throw new MalformedURLException("Unsupported query in profile url");
            }
        }

        @Override
        public void connect() throws IOException {
            assertValid();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            assertValid();
            String path = url.getPath();
            Container container = fabricService.get().getCurrentContainer();
            Profile overlayProfile = container.getOverlayProfile();
            byte[] bytes = overlayProfile.getFileConfiguration(path);
            IllegalStateAssertion.assertNotNull(bytes, "Resource " + path + " does not exist in the profile overlay.");
            return new ByteArrayInputStream(bytes);
        }
    }

    void bindFabricService(FabricService fabricService) {
        this.fabricService.bind(fabricService);
    }

    void unbindFabricService(FabricService fabricService) {
        this.fabricService.unbind(fabricService);
    }
}
