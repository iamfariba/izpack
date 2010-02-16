package com.izforge.izpack.merge;

import org.apache.tools.zip.ZipOutputStream;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.text.StringContains;
import org.hamcrest.text.StringEndsWith;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test a single file merge
 *
 * @author Anthonin Bonnefoy
 */
public class MergeManagerTest {
    private File zip;
    private MergeManagerImpl mergeManager;

    @Before
    public void setUp() {
        zip = new File("outputZip.zip");
        zip.delete();
        mergeManager = new MergeManagerImpl();
    }

    @Test
    public void testMergeSingleFile() throws Exception {
        FileMerge fileMerge = new FileMerge(getClass().getResource("FileMerge.class"));

        doMerge(fileMerge);

        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        assertThat(inputStream.available(), Is.is(1));
        ZipEntry zipEntry = inputStream.getNextEntry();
        assertThat(zipEntry.getName(), Is.is("FileMerge.class"));
    }

    @Test
    public void testMergeDirectory() throws Exception {
        URL url = ClassLoader.getSystemResource("com/izforge/izpack/merge/");
        FileMerge fileMerge = new FileMerge(url);

        ArrayList<String> arrayList = doMerge(fileMerge);

        assertThat(arrayList, IsCollectionContaining.hasItems("merge/test/.placeholder"));
    }

    @Test
    public void testMergeDirectoryWithDestination() throws Exception {
        URL url = new File(getClass().getResource("MergeManagerTest.class").getFile()).getParentFile().toURI().toURL();
        FileMerge fileMerge = new FileMerge(url, "my/dest/path/");

        ArrayList<String> arrayList = doMerge(fileMerge);

        assertThat(arrayList, IsCollectionContaining.hasItems("my/dest/path/MergeManagerTest.class", "my/dest/path/test/.placeholder"));
    }

    private ArrayList<String> getFileNameInZip(File zip) throws IOException {
        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
        ArrayList<String> arrayList = new ArrayList<String>();
        ZipEntry zipEntry;
        while ((zipEntry = inputStream.getNextEntry()) != null) {
            arrayList.add(zipEntry.getName());
        }
        inputStream.close();
        return arrayList;
    }

    private ArrayList<String> doMerge(Mergeable fileMerge) throws IOException {
        ZipOutputStream outputStream = new ZipOutputStream(zip);
        fileMerge.merge(outputStream);
        outputStream.close();
        return getFileNameInZip(zip);
    }

    @Test
    public void testMergeClassFromJarFile() throws Exception {
//        Mergeable jarMerge = PathResolver.getMergeableFromPath("junit/framework/Assert.class");
//        assertThat(jarMerge, Is.is(JarMerge.class));
//
//        ArrayList<String> arrayList = doMerge(jarMerge);
//
//        assertThat(arrayList.size(), Is.is(1));
//        assertThat(arrayList, IsCollectionContaining.hasItem(Is.is("junit/framework/Assert.class")));
    }

    @Test
    public void findFileInJarFoundWithURL() throws Exception {
        URL urlJar = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{urlJar}, ClassLoader.getSystemClassLoader());

        JarMerge jarMerge = new JarMerge(loader.getResource("jar/izforge"));
        File file = jarMerge.find(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().matches(".*HelloPanel\\.class") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is("HelloPanel.class"));
    }

    @Test
    public void mergeJarFoundWithURL() throws Exception {
        URL urlJar = ClassLoader.getSystemResource("com/izforge/izpack/merge/test/jar-hellopanel-1.0-SNAPSHOT.jar");
        URLClassLoader loader = URLClassLoader.newInstance(new URL[]{urlJar}, ClassLoader.getSystemClassLoader());

        JarMerge jarMerge = new JarMerge(loader.getResource("jar/izforge"), "com/dest");
        ArrayList<String> arrayList = doMerge(jarMerge);

        assertThat(arrayList, IsCollectionContaining.hasItems("com/dest/izpack/panels/hello/HelloPanel.class"));
    }


    @Test
    public void testMergePackageFromJar() throws Exception {
//        Mergeable jarMerge = MergeHelper.getMergeableFromPath("junit/framework/");
//        assertThat(jarMerge, Is.is(JarMerge.class));
//
//        doMerge(jarMerge);
//
//        ZipInputStream inputStream = new ZipInputStream(new FileInputStream(zip));
//        ZipEntry zipEntry = inputStream.getNextEntry();
//        assertThat(zipEntry.getName(), Is.is("junit/framework/Assert.class"));
//        zipEntry = inputStream.getNextEntry();
//        assertThat(zipEntry.getName(), Is.is("junit/framework/AssertionFailedError.class"));
    }

    @Test
    public void testGetJarPath() throws Exception {
        String jarPath = MergeManagerImpl.getJarAbsolutePath("junit/framework/Assert.class");
        assertThat(jarPath, StringEndsWith.endsWith("junit-4.7.jar"));
        assertThat(new File(jarPath).exists(), Is.is(true));
    }

    @Test
    public void testGetJarFromPackage() throws Exception {
        String jarPath = MergeManagerImpl.getJarAbsolutePath("junit/framework");
        assertThat(jarPath, StringEndsWith.endsWith("junit-4.7.jar"));
        assertThat(new File(jarPath).exists(), Is.is(true));
    }

    @Test
    public void testProcessJarPath() throws Exception {
        URL resource = new URL("file:/home/test/unjar.jar!com/package/in/jar");
        String jarPath = MergeManagerImpl.processUrlToJarPath(resource);
        System.out.println(jarPath);
        assertThat(jarPath, Is.is("/home/test/unjar.jar"));
    }

    @Test
    public void findFileInDirectory() throws Exception {
//        FileMerge fileMerge = new FileMerge(MergeManagerImpl.getFileFromPath("com/izforge/izpack/merge/test"));
//        File file = fileMerge.find(new FileFilter() {
//            public boolean accept(File pathname) {
//                return pathname.getName().equals(".placeholder") || pathname.isDirectory();
//            }
//        });
//        assertThat(file.getName(), Is.is(".placeholder"));
    }

    @Test
    public void findFileInJar() throws Exception {
        JarMerge jarMerge = new JarMerge("org/junit");
        File file = jarMerge.find(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().matches(".*Assert\\.class") || pathname.isDirectory();
            }
        });
        assertThat(file.getName(), Is.is("Assert.class"));
    }

    @Test
    public void testAddResourceToMerge() throws Exception {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/");

        ArrayList<String> arrayList = doMerge(mergeManager);

        assertThat(arrayList, IsCollectionContaining.hasItems("com/izforge/izpack/merge/MergeManager.class"));
    }

    @Test
    public void testAddResourceToMergeWithDestination() throws Exception {
        mergeManager.addResourceToMerge("com/izforge/izpack/merge/", "com/dest/");

        ArrayList<String> arrayList = doMerge(mergeManager);

        assertThat(arrayList, IsCollectionContaining.hasItems("com/dest/MergeManager.class"));
    }

    @Test
    public void testAddSingleClassToMergeWithDestinationFromAJar() throws Exception {
        mergeManager.addResourceToMerge("org/junit/Assert.class", "com/dest/Assert.class");
        ArrayList<String> arrayList = doMerge(mergeManager);

        assertThat(arrayList, IsCollectionContaining.hasItems("com/dest/Assert.class"));
    }

    @Test
    public void testAddPackageToMergeWithDestinationFromAJar() throws Exception {
        mergeManager.addResourceToMerge("org/junit", "com/dest");
        ArrayList<String> arrayList = doMerge(mergeManager);

        assertThat(arrayList, IsCollectionContaining.hasItems("com/dest/Assert.class"));
    }

    @Test
    public void testAddJarContent() throws Exception {
        JarMerge jarMerge = new JarMerge(getClass().getResource("test/jar-hellopanel-1.0-SNAPSHOT.jar"));
        ArrayList<String> arrayList = doMerge(jarMerge);
        assertThat(arrayList, IsCollectionContaining.hasItems("jar/izforge/izpack/panels/hello/HelloPanel.class"));
    }

    @Test
    public void testFindPanelInJar() throws Exception {
        JarMerge jarMerge = new JarMerge(getClass().getResource("test/izpack-panel-5.0.0-SNAPSHOT.jar"));

        File file = jarMerge.find(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory() ||
                        pathname.getName().replaceAll(".class", "").equalsIgnoreCase("CheckedHelloPanel");
            }
        });
        assertThat(file.getAbsolutePath(), StringContains.containsString("com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class"));
    }


}
