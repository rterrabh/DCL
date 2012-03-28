package dclsuite.asm;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;

import dclsuite.util.DCLUtil;

public class DCLClassReader extends ClassReader{

	public DCLClassReader(InputStream in) throws IOException {
		super(in);
	}
	
	@Override
	public String getClassName() {
		return DCLUtil.adjustClassName(super.getClassName());
	}

}
