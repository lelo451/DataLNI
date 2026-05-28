package com.lni.datalni.ui;

import com.lni.datalni.security.CurrentUser;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;

/** Header strip view-model: shows who is signed in. */
@VariableResolver(DelegatingVariableResolver.class)
public class MainVM {

    @WireVariable private CurrentUser currentUser;

    public String getUsername() {
        return currentUser.getUsername();
    }

    public String getRolesDisplay() {
        return currentUser.getRolesDisplay();
    }
}
