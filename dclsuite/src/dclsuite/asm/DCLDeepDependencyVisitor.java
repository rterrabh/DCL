package dclsuite.asm;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

import dclsuite.dependencies.AccessFieldDependency;
import dclsuite.dependencies.AccessMethodDependency;
import dclsuite.dependencies.AnnotateClassDependency;
import dclsuite.dependencies.AnnotateFieldDependency;
import dclsuite.dependencies.AnnotateMethodDependency;
import dclsuite.dependencies.CreateMethodDependency;
import dclsuite.dependencies.DeclareFieldDependency;
import dclsuite.dependencies.DeclareLocalVariableDependency;
import dclsuite.dependencies.DeclareParameterDependency;
import dclsuite.dependencies.Dependency;
import dclsuite.dependencies.ExtendDirectDependency;
import dclsuite.dependencies.ImplementDirectDependency;
import dclsuite.dependencies.ThrowDependency;


//Talvez um EmptyVisitor

public class DCLDeepDependencyVisitor implements AnnotationVisitor, ClassVisitor, FieldVisitor, MethodVisitor, SignatureVisitor {
	private static final boolean DEBUG = false;
	private List<Dependency> dependencies;
	private int currentLine;
	private MemberType memberType;
	private String className;
	private String methodName;
	private int methodParametersCount;
	private String fieldName;
	private Map<Label, Integer> labels;
	
	public void init(){
		this.currentLine = 0;
		this.methodParametersCount = 0;
		this.memberType = MemberType.CLASS;
		this.dependencies = new LinkedList<Dependency>();
		this.labels = new HashMap<Label, Integer>();
	}
	
	public List<Dependency> getDependencies() {
		return this.dependencies;
	}
	
	/*-------------*/
	/*CLASS VISITOR*/
	/*-------------*/
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.init();

		this.className = name;		
			
		if (DEBUG){
			System.out.println("NAME: " + name );
			System.out.println("\nSUPERCLASS: " + superName);
			System.out.print("\nINTERFACES: ");
			
			if (interfaces!=null && interfaces.length>0){
				System.out.println();
				for (String interfaceName : interfaces){
					System.out.println("            " + interfaceName);
				}
			}else{
				System.out.println("NONE");
			}
			System.out.println();
		}

		this.dependencies.add(new ExtendDirectDependency(this.className,superName,null));
		
		for (String interfaceName : interfaces){
			this.dependencies.add(new ImplementDirectDependency(this.className,interfaceName,null));
		}
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		if (DEBUG){
			System.out.println(this.memberType + " ANNOTATION: " + Type.getType(desc).getClassName() + "\n");
		}
		
		if (MemberType.CLASS.equals(memberType)){
			//Annotation Class Dependency
			this.dependencies.add(new AnnotateClassDependency(this.className,Type.getType(desc).getClassName(),null));
		}else if (MemberType.METHOD.equals(memberType)){
			//Annotation Method Dependency
			this.dependencies.add(new AnnotateMethodDependency(this.className,Type.getType(desc).getClassName(),null,this.methodName));
		}else if (MemberType.FIELD.equals(memberType)){
			//Annotation Field Dependency
			this.dependencies.add(new AnnotateFieldDependency(this.className,Type.getType(desc).getClassName(),null,this.fieldName));
		}
		return this;
	}

	public void visitAttribute(Attribute attr) {
	}

	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		this.memberType = MemberType.FIELD;
		this.fieldName = name;
		if (DEBUG){
			System.out.println("FIELD: " + name + " of type " + Type.getType(desc).getClassName());
		}
		
		//Declare Field Dependency
		this.dependencies.add(new DeclareFieldDependency(this.className,Type.getType(desc).getClassName(),this.currentLine,this.fieldName));
		return this;
	}

	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		Type[] types = Type.getArgumentTypes(desc);
		
		this.memberType = MemberType.METHOD;
		this.methodName = name;
		this.methodParametersCount = types.length;
		
		if (DEBUG){
			System.out.print("\nMETHOD: " + name + "\n");
			System.out.print("EXCEPTIONS: ");
			if (exceptions!=null && exceptions.length>0){
				System.out.println();
				for (String ex : exceptions){
					System.out.println("            " + ex);
				}
			}else{
				System.out.println("NONE");
			}
			System.out.println();
			System.out.print("PARAMETERS: ");
			
			if (types!=null && types.length>0){
				System.out.println();
				for (Type t : types){
					System.out.println("            " + t.getClassName());
				}
			}else{
				System.out.println("NONE");
			}
			System.out.println();
		}
		if (exceptions!=null && exceptions.length>0){
			for (String ex : exceptions){
				//Throw Dependency
				this.dependencies.add(new ThrowDependency(this.className,ex,null,this.methodName));
			}
		}		
		return this;
	}

	public void visitSource(String source, String debug) {
		
	}

	public void visitInnerClass(String name, String outerName,
			String innerName, int access) {
	}

	public void visitOuterClass(String owner, String name, String desc) {
	}

	/*--------------*/
	/*METHOD VISITOR*/
	/*--------------*/
	public AnnotationVisitor visitParameterAnnotation(int parameter,
			String desc, boolean visible) {
		return this;
	}

	public void visitTypeInsn(int opcode, String desc) {
		/*Para visitar um construtor*/
		if (opcode == Opcodes.NEW){
			//Create Dependency
			this.dependencies.add(new CreateMethodDependency(this.className,desc,this.currentLine,
												this.methodName));
		}
	}

	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		if (opcode==Opcodes.GETFIELD){
			//Access Field Dependency
			this.dependencies.add(new AccessFieldDependency(this.className,owner,this.currentLine,
												this.methodName,name,false));
		}else if (opcode==Opcodes.GETSTATIC){
			//Access Field Dependency
			this.dependencies.add(new AccessFieldDependency(this.className,owner,this.currentLine,
												this.methodName,name,true));
		}
		
	}

	public void visitMethodInsn(int opcode, String owner, String name,
			String desc) {
		if (DEBUG){
			System.out.println("LINE: " + currentLine);
			System.out.println("INVOKE: " + owner + "->" + name + " (" + opcode + ")");
			System.out.print("   PARAMETERS: ");
			Type[] types = Type.getArgumentTypes(desc);
			if (types!=null && types.length>0){
				System.out.println();
				for (Type t : types){
					System.out.println("               " + t.getClassName());
				}
			}else{
				System.out.println("   NONE");
			}
			System.out.println();
		}
		
		//This if ignores a call to a own constructor
		if ("<init>".equals(name)) return;
		
		if (opcode == Opcodes.INVOKESPECIAL){
			//Access Method Dependency
			this.dependencies.add(new AccessMethodDependency(this.className,owner,this.currentLine,
												this.methodName,name,false));
		}else if(opcode == Opcodes.INVOKESTATIC){
			//Access Method Dependency
			this.dependencies.add(new AccessMethodDependency(this.className,owner,this.currentLine,
												this.methodName,name,true));
		}else if (opcode == Opcodes.INVOKEVIRTUAL){
			//Access Method Dependency
			this.dependencies.add(new AccessMethodDependency(this.className,owner,this.currentLine,
												this.methodName,name,false));
		}else if (opcode == Opcodes.INVOKEINTERFACE){
			//Access Method Dependency
			this.dependencies.add(new AccessMethodDependency(this.className,owner,this.currentLine,
												this.methodName,name,false));
		}
	}

	public void visitLdcInsn(Object cst) {
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
	}

	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
		if (DEBUG){
			System.out.println("LOCAL VARIABLE: " + name + " of type " + Type.getType(desc).getClassName());
		}
		
		if ("this".equals(name)){
			this.methodParametersCount++;
			return;
		}
		if (index<this.methodParametersCount){
			this.dependencies.add(new DeclareParameterDependency(this.className,Type.getType(desc).getClassName(),labels.get(start)-1,
					this.methodName, name));
		}else{
			//Declare Local Variable Dependency
			this.dependencies.add(new DeclareLocalVariableDependency(this.className,Type.getType(desc).getClassName(),labels.get(start)-1,
									this.methodName, name));
		}
	}

	public AnnotationVisitor visitAnnotationDefault() {
		return this;
	}

	public void visitCode() {
	}

	public void visitInsn(int opcode) {
	}

	public void visitIntInsn(int opcode, int operand) {
	}

	public void visitVarInsn(int opcode, int var) {
	}

	public void visitJumpInsn(int opcode, Label label) {
	}

	public void visitLabel(Label label) {
		labels.put(label, this.currentLine);
	}

	public void visitIincInsn(int var, int increment) {
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels) {
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type) {
	}

	public void visitLineNumber(int line, Label start) {
		labels.put(start, line);
		this.currentLine = line;
	}

	public void visitMaxs(int maxStack, int maxLocals) {
	}

	/*------------------*/
	/*ANNOTATION VISITOR*/
	/*------------------*/
	public void visit(String name, Object value) {
	}

	public void visitEnum(String name, String desc, String value) {
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		return this;
	}

	public AnnotationVisitor visitArray(String name) {
		return this;
	}
	
	
	/*-----------------*/
	/*SIGNATURE VISITOR*/
	/*-----------------*/
	public void visitFormalTypeParameter(String name) {
	}

	public SignatureVisitor visitClassBound() {
		return this;
	}

	public SignatureVisitor visitInterfaceBound() {
		return this;
	}

	public SignatureVisitor visitSuperclass() {
		System.out.println("opa");
		return this;
	}

	public SignatureVisitor visitInterface() {
		System.out.println(currentLine);
		return this;
	}

	public SignatureVisitor visitParameterType() {
		return this;
	}

	public SignatureVisitor visitReturnType() {
		return this;
	}

	public SignatureVisitor visitExceptionType() {
		return this;
	}

	public void visitBaseType(char descriptor) {
	}

	public void visitTypeVariable(String name) {
	}

	public SignatureVisitor visitArrayType() {
		return this;
	}

	public void visitClassType(String name) {
	}

	public void visitInnerClassType(String name) {
	}

	public void visitTypeArgument() {
	}

	public SignatureVisitor visitTypeArgument(char wildcard) {
		return this;
	}
	

	/*------*/
	/*COMMOM*/
	/*------*/
	public void visitEnd() {
	}

	public void visitFrame(int arg0, int arg1, Object[] arg2, int arg3,
			Object[] arg4) {
		
	}

}

enum MemberType{
	FIELD, METHOD, CLASS
}