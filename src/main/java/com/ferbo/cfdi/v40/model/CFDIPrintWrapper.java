package com.ferbo.cfdi.v40.model;

import java.util.Map;

import mx.gob.sat.cfdi.v40.Comprobante;

public class CFDIPrintWrapper {
	private final Comprobante comprobante;
	private final Map<String, Object> jasperParameters;

	public CFDIPrintWrapper(Comprobante comprobante, Map<String, Object> jasperParameters) {
		this.comprobante = comprobante;
		this.jasperParameters = jasperParameters;
	}

	public Comprobante getComprobante() {
		return comprobante;
	}

	public Map<String, Object> getJasperParameters() {
		return jasperParameters;
	}
}
