package org.weasis.dicom.explorer.pref;

import java.util.Hashtable;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.weasis.core.api.gui.Insertable;
import org.weasis.core.api.gui.Insertable.Type;
import org.weasis.core.api.gui.PreferencesPageFactory;
import org.weasis.core.api.gui.util.AbstractItemDialogPage;

@Component(immediate = false)
@Service
public class WadoPrefFactory implements PreferencesPageFactory {

    @Override
    public AbstractItemDialogPage createInstance(Hashtable<String, Object> properties) {
        if (properties != null) {
            if ("superuser".equals(properties.get("weasis.user.prefs"))) { //$NON-NLS-1$ //$NON-NLS-2$
                return new WadoPrefView();
            }
        }
        return null;
    }

    @Override
    public void dispose(Insertable component) {
    }

    @Override
    public boolean isComponentCreatedByThisFactory(Insertable component) {
        return component instanceof WadoPrefView;
    }

    @Override
    public Type getType() {
        return Insertable.Type.PREFERENCES;
    }
}