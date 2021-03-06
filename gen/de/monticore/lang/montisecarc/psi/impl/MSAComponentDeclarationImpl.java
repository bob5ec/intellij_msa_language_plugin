// This is a generated file. Not intended for manual editing.
package de.monticore.lang.montisecarc.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static de.monticore.lang.montisecarc.psi.MSACompositeElementTypes.*;
import de.monticore.lang.montisecarc.psi.impl.mixin.MSAComponentDeclarationImplMixin;
import de.monticore.lang.montisecarc.psi.*;
import de.monticore.lang.montisecarc.stubs.elements.MSAComponentDeclarationStub;
import com.intellij.psi.stubs.IStubElementType;
import static de.monticore.lang.montisecarc.psi.MSATokenElementTypes.*;

public class MSAComponentDeclarationImpl extends MSAComponentDeclarationImplMixin implements MSAComponentDeclaration {

  public MSAComponentDeclarationImpl(ASTNode node) {
    super(node);
  }

  public MSAComponentDeclarationImpl(MSAComponentDeclarationStub stub, IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public void accept(@NotNull MSAVisitor visitor) {
    visitor.visitComponentDeclaration(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MSAVisitor) accept((MSAVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MSAComponentBody getComponentBody() {
    return PsiTreeUtil.getChildOfType(this, MSAComponentBody.class);
  }

  @Override
  @Nullable
  public MSAComponentSignature getComponentSignature() {
    return PsiTreeUtil.getChildOfType(this, MSAComponentSignature.class);
  }

  @Override
  @Nullable
  public MSAStereotype getStereotype() {
    return PsiTreeUtil.getChildOfType(this, MSAStereotype.class);
  }

  @Override
  @Nullable
  public MSASuppressAnnotation getSuppressAnnotation() {
    return PsiTreeUtil.getChildOfType(this, MSASuppressAnnotation.class);
  }

  @NotNull
  public String getQualifiedName() {
    return MSAPsiImplUtil.getQualifiedName(this);
  }

  @NotNull
  public String getComponentName() {
    return MSAPsiImplUtil.getComponentName(this);
  }

  @NotNull
  public String getInstanceName() {
    return MSAPsiImplUtil.getInstanceName(this);
  }

  public int getTrustLevel() {
    return MSAPsiImplUtil.getTrustLevel(this);
  }

  public int getAbsoluteTrustLevel() {
    return MSAPsiImplUtil.getAbsoluteTrustLevel(this);
  }

}
