/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.dicom.codec.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.service.AuditLog;
import org.weasis.core.api.service.BundlePreferences;
import org.weasis.core.api.service.ImageioUtil;
import org.weasis.dicom.codec.DicomCodec;
import org.weasis.dicom.codec.DicomMediaIO;
import org.weasis.dicom.codec.DicomSpecialElementFactory;
import org.weasis.dicom.codec.pref.DicomPrefManager;

public class Activator implements BundleActivator, ServiceListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private static final String LOGGER_KEY = "always.info.ItemParser";
    private static final String LOGGER_VAL = "org.dcm4che3.imageio.ItemParser";

    // @Override
    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        // Register SPI in imageio registry with the classloader of this bundle (provides also the classpath for
        // discovering the SPI files). Here are the codecs:
        // org.dcm4che3.imageioimpl.plugins.rle.RLEImageReaderSpi
        // org.dcm4che3.imageioimpl.plugins.dcm.DicomImageReaderSpi
        // org.dcm4che3.imageioimpl.plugins.dcm.DicomImageWriterSpi
        ImageioUtil.registerServiceProvider(DicomCodec.RLEImageReaderSpi);
        ImageioUtil.registerServiceProvider(DicomCodec.DicomImageReaderSpi);

        ConfigurationAdmin confAdmin = BundlePreferences.getService(bundleContext, ConfigurationAdmin.class);
        if (confAdmin != null) {
            Configuration logConfiguration = AuditLog.getLogConfiguration(confAdmin, LOGGER_KEY, LOGGER_VAL);
            if (logConfiguration == null) {
                logConfiguration =
                    confAdmin
                        .createFactoryConfiguration("org.apache.sling.commons.log.LogManager.factory.config", null);
                Dictionary<String, Object> loggingProperties = new Hashtable<String, Object>();
                loggingProperties.put("org.apache.sling.commons.log.level", "INFO");
                // loggingProperties.put("org.apache.sling.commons.log.file", "logs.log");
                loggingProperties.put("org.apache.sling.commons.log.names", LOGGER_VAL);
                // add this property to give us something unique to re-find this configuration
                loggingProperties.put(LOGGER_KEY, LOGGER_VAL);
                logConfiguration.update(loggingProperties);
            }
        }

        try {
            for (ServiceReference<DicomSpecialElementFactory> service : bundleContext.getServiceReferences(
                DicomSpecialElementFactory.class, null)) {
                DicomSpecialElementFactory factory = bundleContext.getService(service);
                if (factory != null) {
                    for (String modality : factory.getModalities()) {
                        DicomSpecialElementFactory prev = DicomMediaIO.DCM_ELEMENT_FACTORIES.put(modality, factory);
                        if (prev != null) {
                            LOGGER.warn("{} factory has been replaced by {}", prev.getClass(), factory.getClass());
                        }
                        LOGGER.debug("Register DicomSpecialElementFactory: {}", factory.getClass());
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

        bundleContext.addServiceListener(this,
            String.format("(%s=%s)", Constants.OBJECTCLASS, DicomSpecialElementFactory.class.getName()));//$NON-NLS-1$
    }

    // @Override
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        DicomPrefManager.getInstance().savePreferences();
        ImageioUtil.deregisterServiceProvider(DicomCodec.RLEImageReaderSpi);
        ImageioUtil.deregisterServiceProvider(DicomCodec.DicomImageReaderSpi);
    }

    @Override
    public synchronized void serviceChanged(final ServiceEvent event) {
        ServiceReference<?> m_ref = event.getServiceReference();
        BundleContext context = FrameworkUtil.getBundle(Activator.this.getClass()).getBundleContext();
        DicomSpecialElementFactory factory = (DicomSpecialElementFactory) context.getService(m_ref);
        if (factory == null) {
            return;
        }

        if (event.getType() == ServiceEvent.REGISTERED) {
            for (String modality : factory.getModalities()) {
                DicomSpecialElementFactory prev = DicomMediaIO.DCM_ELEMENT_FACTORIES.put(modality, factory);
                if (prev != null) {
                    LOGGER.warn("{} factory has been replaced by {}", prev.getClass(), factory.getClass());
                }
                LOGGER.debug("Register DicomSpecialElementFactory: {}", factory.getClass());
            }

        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            for (String modality : factory.getModalities()) {
                DicomSpecialElementFactory f = DicomMediaIO.DCM_ELEMENT_FACTORIES.get(modality);
                if (factory.equals(f)) {
                    DicomMediaIO.DCM_ELEMENT_FACTORIES.remove(modality);
                } else {
                    LOGGER.warn("Cannot unregister {}, {} is registered instead", factory.getClass(), f.getClass());
                }
                LOGGER.debug("Unregister DicomSpecialElementFactory: {}", factory.getClass());
            }
            // Unget service object and null references.
            context.ungetService(m_ref);
        }

    }
}
