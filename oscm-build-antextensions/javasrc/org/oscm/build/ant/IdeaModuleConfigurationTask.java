/*x********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2021
 *
 * Creation Date: 07.05.2021
 *
 *******************************************************************************x*/
package org.oscm.build.ant;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/** @author goebel */
public class IdeaModuleConfigurationTask extends Task {

  private String fileName;
  private String dirName;
  private File workspace;

  public void setWorkspace(File file) {
    workspace = file;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getDirName() {
    return dirName;
  }

  public void setDirName(String dirName) {
    this.dirName = dirName;
  }

  @Override
  public void execute() throws BuildException {

    if (workspace == null) throw new BuildException("No workspace location set.");
    if (dirName == null) throw new BuildException("No idea metadata location set.");
    if (fileName == null || !(fileName.endsWith(".iml"))) {
      throw new BuildException("No .iml file set");
    }
    final File metafile = new File(dirName, fileName);
    if (!metafile.exists()) {
      System.out.printf("Skip IDEA modules. Metafile %s not found.\n", metafile.getPath());
      return;
    }

    ensureOutDir(workspace);

    try {
      Node contentNode = readXML(workspace, dirName, fileName);
      updateMetaInfos(contentNode, workspace, dirName, fileName);
    } catch (SAXException | ParserConfigurationException | IOException e) {
      e.printStackTrace();
    }
  }

  private void ensureOutDir(File root) {
    File out = new File(root, "out");
    if (!out.exists()) {
      out.mkdirs();
    }
  }

  protected Node readXML(File wsRoot, String parent, String file)
      throws SAXException, ParserConfigurationException, IOException {
    String content = readFile(new File(parent, file));
    Document doc = XMLHelper.convertToDocument(content, false);
    return doc.getElementsByTagName("content").item(0);
  }

  protected void updateMetaInfos(Node node, File wsRoot, String metaFolder, String file) {

    removeOldSourceFolderRefs(node);

    Document doc = node.getOwnerDocument();
    System.out.printf("Root: %s\n", wsRoot.getAbsolutePath());

    final EclipseProjectReader reader = new EclipseProjectReader(wsRoot);
    for (String name : reader.getProjectNames()) {
      Project prj = new Project(new File(wsRoot, name));
      for (String src : prj.getSrcPaths()) {
        appendSourceFolderRef(node, doc, prj, src);
      }
    }
    XMLHelper.writeToFS(doc, new File(metaFolder, file).getAbsolutePath());
  }

  void appendSourceFolderRef(Node node, Document doc, Project prj, String src) {
    Element elm = doc.createElement("sourceFolder");
    elm.setAttribute("url", src);
    elm.setAttribute("isTestSource", String.valueOf(prj.isTest()));
    node.appendChild(elm);
  }

  private static String readFile(File sourceFile) throws IOException {
    try (BufferedInputStream inBuf = new BufferedInputStream(new FileInputStream(sourceFile));
        ByteArrayOutputStream outBuf = new ByteArrayOutputStream()) {
      byte[] b = new byte[1024];
      int len;
      while ((len = inBuf.read(b)) != -1) {
        outBuf.write(b, 0, len);
      }
      outBuf.flush();
      return outBuf.toString("UTF-8");
    }
  }

  @SuppressWarnings("boxing")
  private void removeOldSourceFolderRefs(Node node) {
    List<Node> nodes = XMLHelper.getChildrenByTag(node, "sourceFolder");
    System.out.printf("Remove %s sourceFolder nodes\n", nodes.size());
    for (Node n : nodes) {
      node.removeChild(n);
    }

    nodes = XMLHelper.getChildrenByTag(node, "sourceFolder");
    System.out.printf("Now %s sourceFolder nodes\n", nodes.size());
  }

  @SuppressWarnings("Convert2MethodRef")
  static class Project {
    List<String> srcPaths = new ArrayList<>();
    final List<String> srcDirs =
        Arrays.asList(
            new String[] {"javares", "javasrc", "javares-it", "javasrc-it", "WebContent"});
    final FilenameFilter filter =
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            if (new File(dir, name).isDirectory()) {
              return srcDirs.contains(name);
            }
            return false;
          }
        };

    private File prj;

    Project(File prj) {
      this.prj = prj;
      collectSrcPaths(prj, srcPaths);
    }

    void collectSrcPaths(File parent, List<String> paths) {
      paths.addAll(
          Arrays.stream(parent.listFiles(filter))
              .map(f -> relPath(f))
              .collect(Collectors.toList()));
    }

    String relPath(File child) {
      return prj.toURI().relativize(child.toURI()).getPath();
    }

    List<String> getSrcPaths() {
      return srcPaths.stream().map(s -> folderPath(s)).collect(Collectors.toList());
    }

    String folderPath(String child) {
      return "file://$MODULE_DIR$/" + prj.getName() + "/" + child;
    }

    boolean isTest() {
      return prj.getName().endsWith("-unittests") || prj.getName().endsWith("-tests");
    }
  }
}
