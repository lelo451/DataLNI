package com.lni.datalni.ui.support;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Bridges {@code java.time.LocalDate} (DTO side) with {@code java.util.Date}
 * (ZK {@code <datebox>}'s native type, since ZK 9 CE doesn't ship LocalDatebox).
 *
 * <p>Use as: {@code value="@bind(vm.dto.published) @converter('com.lni.datalni.ui.support.LocalDateConverter')"}.
 */
public class LocalDateConverter implements Converter<Object, Object, Component> {

    @Override
    public Object coerceToUi(Object beanProp, Component component, BindContext ctx) {
        if (beanProp == null) {
            return null;
        }
        if (beanProp instanceof LocalDate) {
            return java.sql.Date.valueOf((LocalDate) beanProp);
        }
        return beanProp;
    }

    @Override
    public Object coerceToBean(Object compAttr, Component component, BindContext ctx) {
        if (compAttr == null) {
            return null;
        }
        if (compAttr instanceof Date) {
            // System default zone is enough — DataLniApplication pins it to UTC.
            return ((Date) compAttr).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return compAttr;
    }
}
