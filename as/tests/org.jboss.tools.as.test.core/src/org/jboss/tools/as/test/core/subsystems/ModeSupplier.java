package org.jboss.tools.as.test.core.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;

public class ModeSupplier extends ParameterSupplier {
	
	 public ModeSupplier() {
		super();
	}

	@Override
	 public List<PotentialAssignment> getValueSources(
	   ParameterSignature signature) {
		 
		  List<PotentialAssignment> result = new ArrayList<PotentialAssignment>();
		  
		  result.add(PotentialAssignment.forValue("local", "local" ));
		  result.add(PotentialAssignment.forValue("rse", "rse" ));
		  result.add(PotentialAssignment.forValue("null", null ));
		  return result;
	 }
}