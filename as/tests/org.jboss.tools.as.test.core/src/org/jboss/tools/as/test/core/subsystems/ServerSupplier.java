package org.jboss.tools.as.test.core.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.jboss.tools.as.test.core.internal.utils.ServerParameterUtils;
import org.junit.experimental.theories.ParameterSignature;
import org.junit.experimental.theories.ParameterSupplier;
import org.junit.experimental.theories.PotentialAssignment;

public class ServerSupplier extends ParameterSupplier {

 public ServerSupplier() {
	super();
}

@Override
 public List<PotentialAssignment> getValueSources(
   ParameterSignature signature) {

  List<PotentialAssignment> result = new ArrayList<PotentialAssignment>();

  String[] servers = ServerParameterUtils.getAllJBossServerTypeParameters();
  for (String string : servers) {
	  result.add(PotentialAssignment.forValue(string, string ));
  }
  return result;
 }
}