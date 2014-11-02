package dclsuite.ast;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import dclsuite.dependencies.AccessFieldDependency;
import dclsuite.dependencies.AccessMethodDependency;
import dclsuite.dependencies.AnnotateClassDependency;
import dclsuite.dependencies.AnnotateFieldDependency;
import dclsuite.dependencies.AnnotateFormalParameterDependency;
import dclsuite.dependencies.AnnotateMethodDependency;
import dclsuite.dependencies.AnnotateVariableDependency;
import dclsuite.dependencies.CreateFieldDependency;
import dclsuite.dependencies.CreateMethodDependency;
import dclsuite.dependencies.DeclareCastDependency;
import dclsuite.dependencies.DeclareFieldDependency;
import dclsuite.dependencies.DeclareInstanceOfDependency;
import dclsuite.dependencies.DeclareLocalVariableDependency;
import dclsuite.dependencies.DeclareParameterDependency;
import dclsuite.dependencies.DeclareParameterizedTypeDependency;
import dclsuite.dependencies.DeclareReturnDependency;
import dclsuite.dependencies.Dependency;
import dclsuite.dependencies.ExtendDirectDependency;
import dclsuite.dependencies.ExtendIndirectDependency;
import dclsuite.dependencies.ImplementDirectDependency;
import dclsuite.dependencies.ImplementIndirectDependency;
import dclsuite.dependencies.ThrowDependency;
import dclsuite.exception.DCLException;

public class DCLDeepDependencyVisitor extends ASTVisitor {
	private List<Dependency> dependencies;

	private ICompilationUnit unit;
	private CompilationUnit fullClass;
	private String className;

	public DCLDeepDependencyVisitor(ICompilationUnit unit) throws DCLException {
		try{
			this.dependencies = new ArrayList<Dependency>();
			this.unit = unit;
	
			this.className = unit.getParent().getElementName() + "." + unit.getElementName().substring(0, unit.getElementName().length() - 5);
			ASTParser parser = ASTParser.newParser(AST.JLS4); // It was JSL3, but it
																// is now deprecated
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(unit);
			parser.setResolveBindings(true);
	
			this.fullClass = (CompilationUnit) parser.createAST(null); // parse
			this.fullClass.accept(this);
		} catch(Exception e){
			throw new DCLException(e,unit);
		}
	}

	public final List<Dependency> getDependencies() {
		return this.dependencies;
	}

	public final String getClassName() {
		return this.className;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (!node.isLocalTypeDeclaration() && !node.isMemberTypeDeclaration()) { // Para
																					// evitar
																					// fazer
																					// v�rias
																					// vezes
			try {
				IType type = (IType) unit.getTypes()[0];
				ITypeHierarchy typeHierarchy = type.newSupertypeHierarchy(null);

				IType[] typeSuperclasses = typeHierarchy.getAllSuperclasses(type);

				for (IType t : typeSuperclasses) {
					if (node.getSuperclassType() != null
							&& t.getFullyQualifiedName().equals(node.getSuperclassType().resolveBinding().getQualifiedName())) {
						this.dependencies.add(new ExtendDirectDependency(this.className, t.getFullyQualifiedName(), fullClass
								.getLineNumber(node.getSuperclassType().getStartPosition()), node.getSuperclassType().getStartPosition(),
								node.getSuperclassType().getLength()));
					} else {
						this.dependencies.add(new ExtendIndirectDependency(this.className, t.getFullyQualifiedName(), null, null, null));
					}
				}

				IType[] typeSuperinter = typeHierarchy.getAllInterfaces();

				externo: for (IType t : typeSuperinter) {
					for (Object it : node.superInterfaceTypes()) {
						switch (((Type) it).getNodeType()) {
						case ASTNode.SIMPLE_TYPE:
							SimpleType st = (SimpleType) it;
							if (t.getFullyQualifiedName().equals(st.getName().resolveTypeBinding().getQualifiedName())) {
								if (!type.isInterface()) {
									this.dependencies.add(new ImplementDirectDependency(this.className, t.getFullyQualifiedName(),
											fullClass.getLineNumber(st.getStartPosition()), st.getStartPosition(), st.getLength()));
								} else {
									this.dependencies.add(new ExtendDirectDependency(this.className, t.getFullyQualifiedName(), fullClass
											.getLineNumber(st.getStartPosition()), st.getStartPosition(), st.getLength()));
								}
								continue externo;
							}
							break;
						case ASTNode.PARAMETERIZED_TYPE:
							ParameterizedType pt = (ParameterizedType) it;
							if (t!= null && t.getFullyQualifiedName() != null && pt != null && pt.getType() != null && pt.getType().resolveBinding() != null &&
									t.getFullyQualifiedName().equals(pt.getType().resolveBinding().getBinaryName())) {
								if (!type.isInterface()) {
									this.dependencies.add(new ImplementDirectDependency(this.className, t.getFullyQualifiedName(),
											fullClass.getLineNumber(pt.getStartPosition()), pt.getStartPosition(), pt.getLength()));
								} else {
									this.dependencies.add(new ExtendDirectDependency(this.className, t.getFullyQualifiedName(), fullClass
											.getLineNumber(pt.getStartPosition()), pt.getStartPosition(), pt.getLength()));
								}
								continue externo;
							}
							break;
						}
					}
					this.dependencies.add(new ImplementIndirectDependency(this.className, t.getFullyQualifiedName(), null, null, null));
				}
			} catch (JavaModelException e) {
				throw new RuntimeException("AST Parser error.", e);
			}
		}
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			FieldDeclaration field = (FieldDeclaration) node.getParent();
			this.dependencies.add(new AnnotateFieldDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(),
					((VariableDeclarationFragment) field.fragments().get(0)).getName().getIdentifier()));
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			MethodDeclaration method = (MethodDeclaration) node.getParent();
			this.dependencies.add(new AnnotateMethodDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(), method.getName()
							.getIdentifier()));
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies.add(new AnnotateClassDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		} else if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			VariableDeclarationStatement st = (VariableDeclarationStatement) node.getParent();
			VariableDeclarationFragment vdf = ((VariableDeclarationFragment) st.fragments().get(0));
			ASTNode relevantParent = this.getRelevantParent(node);
			if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				MethodDeclaration md = (MethodDeclaration) relevantParent;
				this.dependencies.add(new AnnotateVariableDependency(this.className, node.getTypeName().resolveTypeBinding()
						.getQualifiedName(), fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(),
						md.getName().getIdentifier(), vdf.getName().getIdentifier()));
			}
		} else if (node.getParent().getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			SingleVariableDeclaration sv = (SingleVariableDeclaration) node.getParent();
			ASTNode relevantParent = this.getRelevantParent(node);
			if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				MethodDeclaration md = (MethodDeclaration) relevantParent;
				this.dependencies.add(new AnnotateFormalParameterDependency(this.className, node.getTypeName().resolveTypeBinding()
						.getQualifiedName(), fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(),
						md.getName().getIdentifier(), sv.getName().getIdentifier()));
			}

		}
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			FieldDeclaration field = (FieldDeclaration) node.getParent();
			this.dependencies.add(new AnnotateFieldDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(),
					((VariableDeclarationFragment) field.fragments().get(0)).getName().getIdentifier()));
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			MethodDeclaration method = (MethodDeclaration) node.getParent();
			this.dependencies.add(new AnnotateMethodDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(), method.getName()
							.getIdentifier()));
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies.add(new AnnotateClassDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.FIELD_DECLARATION:
			FieldDeclaration fd = (FieldDeclaration) relevantParent;
			this.dependencies.add(new CreateFieldDependency(this.className, this.getTargetClassName(node.getType().resolveBinding()),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(),
					((VariableDeclarationFragment) fd.fragments().get(0)).getName().getIdentifier()));
			break;
		case ASTNode.METHOD_DECLARATION:
			MethodDeclaration md = (MethodDeclaration) relevantParent;
			this.dependencies.add(new CreateMethodDependency(this.className, this.getTargetClassName(node.getType().resolveBinding()),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(), md.getName()
							.getIdentifier()));
			break;
		case ASTNode.INITIALIZER:
			this.dependencies
					.add(new CreateMethodDependency(this.className, this.getTargetClassName(node.getType().resolveBinding()), fullClass
							.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(), "initializer static block"));
			break;
		}

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		this.dependencies.add(new DeclareFieldDependency(this.className, this.getTargetClassName(node.getType().resolveBinding()),
				fullClass.getLineNumber(node.getType().getStartPosition()), node.getType().getStartPosition(), node.getType().getLength(),
				((VariableDeclarationFragment) node.fragments().get(0)).getName().getIdentifier()));
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		for (Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
				this.dependencies.add(new DeclareParameterDependency(this.className, this
						.getTargetClassName(svd.getType().resolveBinding()), fullClass.getLineNumber(svd.getStartPosition()), svd
						.getStartPosition(), svd.getLength(), node.getName().getIdentifier(), svd.getName().getIdentifier()));
				if (svd.getType().getNodeType() == Type.PARAMETERIZED_TYPE) {
					// TODO: Adjust the way that we handle parameter types
					for (Object t : ((ParameterizedType) svd.getType()).typeArguments()) {
						if (t instanceof SimpleType) {
							SimpleType st = (SimpleType) t;
							this.dependencies.add(new DeclareParameterDependency(this.className, this.getTargetClassName(st
									.resolveBinding()), fullClass.getLineNumber(st.getStartPosition()), st.getStartPosition(), st
									.getLength(), node.getName().getIdentifier(), svd.getName().getIdentifier()));
						} else if (t instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) t;
							this.dependencies.add(new DeclareParameterDependency(this.className, this.getTargetClassName(pt.getType()
									.resolveBinding()), fullClass.getLineNumber(pt.getStartPosition()), pt.getStartPosition(), pt
									.getLength(), node.getName().getIdentifier(), svd.getName().getIdentifier()));
						}
					}
				}

			}
		}
		for (Object o : node.thrownExceptions()) {
			Name name = (Name) o;
			this.dependencies.add(new ThrowDependency(this.className, this.getTargetClassName(name.resolveTypeBinding()), fullClass
					.getLineNumber(name.getStartPosition()), name.getStartPosition(), name.getLength(), node.getName().getIdentifier()));
		}

		if (node.getReturnType2() != null
				&& !(node.getReturnType2().isPrimitiveType() && ((PrimitiveType) node.getReturnType2()).getPrimitiveTypeCode() == PrimitiveType.VOID)) {
			if (!node.getReturnType2().resolveBinding().isTypeVariable()) {
				this.dependencies.add(new DeclareReturnDependency(this.className, this.getTargetClassName(node.getReturnType2()
						.resolveBinding()), fullClass.getLineNumber(node.getReturnType2().getStartPosition()), node.getReturnType2()
						.getStartPosition(), node.getReturnType2().getLength(), node.getName().getIdentifier()));
			} else {
				if (node.getReturnType2().resolveBinding().getTypeBounds().length >= 1) {
					this.dependencies.add(new DeclareReturnDependency(this.className, this.getTargetClassName(node.getReturnType2()
							.resolveBinding().getTypeBounds()[0]), fullClass.getLineNumber(node.getReturnType2().getStartPosition()), node
							.getReturnType2().getStartPosition(), node.getReturnType2().getLength(), node.getName().getIdentifier()));
				}
			}

		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			MethodDeclaration md = (MethodDeclaration) relevantParent;

			this.dependencies.add(new DeclareLocalVariableDependency(this.className, this.getTargetClassName(node.getType()
					.resolveBinding()), fullClass.getLineNumber(node.getStartPosition()), node.getType().getStartPosition(), node.getType()
					.getLength(), md.getName().getIdentifier(), ((VariableDeclarationFragment) node.fragments().get(0)).getName()
					.getIdentifier()));

			break;
		case ASTNode.INITIALIZER:
			this.dependencies.add(new DeclareLocalVariableDependency(this.className, this.getTargetClassName(node.getType()
					.resolveBinding()), fullClass.getLineNumber(node.getStartPosition()), node.getType().getStartPosition(), node.getType()
					.getLength(), "initializer static block", ((VariableDeclarationFragment) node.fragments().get(0)).getName()
					.getIdentifier()));
			break;
		}

		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		ASTNode relevantParent = getRelevantParent(node);

		int isStatic = node.resolveMethodBinding().getModifiers() & Modifier.STATIC;

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			MethodDeclaration md = (MethodDeclaration) relevantParent;
			if (node.getExpression() != null) {
				this.dependencies.add(new AccessMethodDependency(this.className, this.getTargetClassName(node.getExpression()
						.resolveTypeBinding()), fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
						node.getLength(), md.getName().getIdentifier(), node.getName().getIdentifier(), isStatic != 0));
			}
			break;
		case ASTNode.INITIALIZER:
			if (node.getExpression() != null) {
				this.dependencies.add(new AccessMethodDependency(this.className, this.getTargetClassName(node.getExpression()
						.resolveTypeBinding()), fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
						node.getLength(), "initializer static block", node.getName().getIdentifier(), isStatic != 0));
			}
			break;
		}
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		ASTNode relevantParent = getRelevantParent(node);

		int isStatic = node.resolveFieldBinding().getModifiers() & Modifier.STATIC;

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			MethodDeclaration md = (MethodDeclaration) relevantParent;
			this.dependencies.add(new AccessFieldDependency(this.className, this.getTargetClassName(node.getExpression()
					.resolveTypeBinding()), fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(), md
					.getName().getFullyQualifiedName(), node.getName().getFullyQualifiedName(), isStatic != 0));
			break;
		case ASTNode.INITIALIZER:
			this.dependencies.add(new AccessFieldDependency(this.className, this.getTargetClassName(node.getExpression()
					.resolveTypeBinding()), fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(),
					"initializer static block", node.getName().getFullyQualifiedName(), isStatic != 0));
			break;
		}
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		if ((node.getParent().getNodeType() == ASTNode.METHOD_INVOCATION || node.getParent().getNodeType() == ASTNode.INFIX_EXPRESSION
				|| node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT || node.getParent().getNodeType() == ASTNode.ASSIGNMENT)
				&& node.getQualifier().getNodeType() != ASTNode.QUALIFIED_NAME) {
			ASTNode relevantParent = getRelevantParent(node);
			int isStatic = node.resolveBinding().getModifiers() & Modifier.STATIC;

			switch (relevantParent.getNodeType()) {
			case ASTNode.METHOD_DECLARATION:
				MethodDeclaration md = (MethodDeclaration) relevantParent;
				this.dependencies.add(new AccessFieldDependency(this.className, this.getTargetClassName(node.getQualifier()
						.resolveTypeBinding()), fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
						node.getLength(), md.getName().getFullyQualifiedName(), node.getName().getFullyQualifiedName(), isStatic != 0));
				break;
			case ASTNode.INITIALIZER:
				this.dependencies.add(new AccessFieldDependency(this.className, this.getTargetClassName(node.getQualifier()
						.resolveTypeBinding()), fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(),
						node.getLength(), "initializer static block", node.getName().getFullyQualifiedName(), isStatic != 0));
				break;
			}

		}

		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return super.visit(node);
	}

	public boolean visit(org.eclipse.jdt.core.dom.NormalAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			FieldDeclaration field = (FieldDeclaration) node.getParent();
			this.dependencies.add(new AnnotateFieldDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(),
					((VariableDeclarationFragment) field.fragments().get(0)).getName().getIdentifier()));
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			MethodDeclaration method = (MethodDeclaration) node.getParent();
			this.dependencies.add(new AnnotateMethodDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength(), method.getName()
							.getIdentifier()));
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies.add(new AnnotateClassDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					fullClass.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		}
		return true;
	};

	@Override
	public boolean visit(ParameterizedType node) {
		ASTNode relevantParent = this.getRelevantParent(node);
		if (node.getNodeType() == ASTNode.PARAMETERIZED_TYPE) {
			ParameterizedType pt = (ParameterizedType) node;
			if (pt.typeArguments() != null) {
				for (Object o : pt.typeArguments()) {
					Type t = (Type) o;
					if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
						MethodDeclaration md = (MethodDeclaration) relevantParent;
						this.dependencies.add(new DeclareParameterizedTypeDependency(this.className, this.getTargetClassName(t
								.resolveBinding()), fullClass.getLineNumber(t.getStartPosition()), t.getStartPosition(), t.getLength(),md.getName().getIdentifier()));
					}else{
						this.dependencies.add(new DeclareParameterizedTypeDependency(this.className, this.getTargetClassName(t
								.resolveBinding()), fullClass.getLineNumber(t.getStartPosition()), t.getStartPosition(), t.getLength()));
					}
				}
			}
		}
		return true;
	}

	private ASTNode getRelevantParent(final ASTNode node) {
		for (ASTNode aux = node; aux != null; aux = aux.getParent()) {
			switch (aux.getNodeType()) {
			case ASTNode.FIELD_DECLARATION:
			case ASTNode.METHOD_DECLARATION:
			case ASTNode.INITIALIZER:
				return aux;
			}
		}
		return node;
	}

	private String getTargetClassName(ITypeBinding type) {
		String result = "";
		if (!type.isAnonymous() && type.getQualifiedName() != null && !type.getQualifiedName().isEmpty()) {
			result = type.getQualifiedName();
		} else if (type.isLocal() && type.getName() != null && !type.getName().isEmpty()) {
			result = type.getName();
		} else if (!type.getSuperclass().getQualifiedName().equals("java.lang.Object") || type.getInterfaces() == null
				|| type.getInterfaces().length == 0) {
			result = type.getSuperclass().getQualifiedName();
		} else if (type.getInterfaces() != null && type.getInterfaces().length == 1) {
			result = type.getInterfaces()[0].getQualifiedName();
		}

		if (result.equals("")) {
			throw new RuntimeException("AST Parser error.");
		} else if (result.endsWith("[]")) {
			result = result.substring(0, result.length() - 2);
		} else if (result.matches(".*<.*>")) {
			result = result.replaceAll("<.*>", "");
		}

		return result;
	}
	
	@Override
	public boolean visit(CastExpression node) {
		Type t = node.getType();
		this.dependencies.add(new DeclareCastDependency(this.className, this.getTargetClassName(t
				.resolveBinding()), fullClass.getLineNumber(t.getStartPosition()), t.getStartPosition(), t.getLength()));
		
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		ITypeBinding typeBinding = node.getRightOperand().resolveBinding();
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			MethodDeclaration md = (MethodDeclaration) relevantParent;
			this.dependencies.add(new DeclareInstanceOfDependency(this.className, this.getTargetClassName(typeBinding), 
					fullClass.getLineNumber(node.getRightOperand().getStartPosition()), node.getRightOperand().getStartPosition(), 
					node.getRightOperand().getLength(),
					md.getName().getIdentifier()));
			break;
		case ASTNode.INITIALIZER:
			this.dependencies.add(new DeclareInstanceOfDependency(this.className, this.getTargetClassName(typeBinding), 
					fullClass.getLineNumber(node.getRightOperand().getStartPosition()), node.getRightOperand().getStartPosition(), 
					node.getRightOperand().getLength(),
					"initializer static block"));
			break;
		}
		return super.visit(node);
		
	}
	
}
