package com.intellij.lang.ant.psi.impl.reference;

import com.intellij.lang.ant.psi.AntFile;
import com.intellij.lang.ant.psi.AntImport;
import com.intellij.lang.ant.psi.AntProject;
import com.intellij.lang.ant.psi.AntStructuredElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSetBase;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.GenericReferenceProvider;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

public class AntFileReferenceSet extends FileReferenceSetBase {

  private final XmlAttributeValue myValue;

  public AntFileReferenceSet(final AntStructuredElement element, final XmlAttributeValue value, final GenericReferenceProvider provider) {
    super(FileUtil.toSystemIndependentName(StringUtil.stripQuotesAroundValue(value.getText())), element,
          value.getTextRange().getStartOffset() - element.getTextRange().getStartOffset() + 1, provider, true);
    myValue = value;
  }

  protected AntFileReference createFileReference(final TextRange range, final int index, final String text) {
    return new AntFileReference(this, range, index, text);
  }

  public AntStructuredElement getElement() {
    return (AntStructuredElement)super.getElement();
  }

  @Nullable
  public String getPathString() {
    return getElement().computeAttributeValue(super.getPathString());
  }

  public boolean isAbsolutePathReference() {
    return super.isAbsolutePathReference() || new File(getPathString()).isAbsolute();
  }

  @NotNull
  public Collection<PsiFileSystemItem> computeDefaultContexts() {
    final AntStructuredElement element = getElement();
    final AntFile file = element.getAntFile();
    if (file != null) {
      final AntProject project = file.getAntProject();
      if (project != null) {
        final String path = project.getBaseDir();
        VirtualFile vFile = file.getContainingPath();
        if (vFile != null) {
          if (path != null && !(element instanceof AntImport)) {
            vFile = LocalFileSystem.getInstance()
              .findFileByPath((new File(vFile.getPath(), path)).getAbsolutePath().replace(File.separatorChar, '/'));
          }
          if (vFile != null) {
            final PsiDirectory directory = file.getViewProvider().getManager().findDirectory(vFile);
            if (directory != null) {
              return Collections.<PsiFileSystemItem>singleton(directory);
            }
          }
        }
      }
    }
    return super.computeDefaultContexts();
  }

  XmlAttributeValue getManipulatorElement() {
    return myValue;
  }
}
