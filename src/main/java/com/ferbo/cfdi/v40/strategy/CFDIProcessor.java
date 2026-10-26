package com.ferbo.cfdi.v40.strategy;

import java.util.Map;

import com.ferbo.cfdi.v40.model.TipoCFDI;

import mx.gob.sat.cfdi.v40.Comprobante;

public interface CFDIProcessor {
	// Le dice a la fábrica qué tipo de documento maneja esta clase
	TipoCFDI getTipoSoportado();

	// Aquí ejecutas las reglas, cálculos o guardado en BD específico
	void procesar(Comprobante comprobante);

	// Procesa el negocio y llena el mapa con sus datasources independientes
	void procesar(Comprobante comprobante, Map<String, Object> parametrosJasper);
}
