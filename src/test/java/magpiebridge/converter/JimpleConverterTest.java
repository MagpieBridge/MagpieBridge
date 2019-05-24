package magpiebridge.converter;

import static org.junit.Assert.assertTrue;

import de.upb.soot.DefaultFactories;
import de.upb.soot.core.SootClass;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.types.JavaClassType;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import soot.G;
import soot.Scene;
import soot.options.Options;

/** @author Linghui Luo */
public class JimpleConverterTest {

  private WalaClassLoader loader;
  private DefaultFactories defaultFactories;
  private JavaClassType declareClassSig;
  private SootClass klass;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/wala-tests/";
    loader = new WalaClassLoader(srcDir, null);
    defaultFactories = DefaultFactories.create();
  }

  @After
  public void convertJimple() {
    // set up soot options
    G.v().reset();
    Options.v().set_whole_program(true);
    Options.v().setPhaseOption("cg.spark", "on");
    Options.v().set_print_tags_in_output(true);
    Scene.v().loadDynamicClasses();
    // load basic classes from soot
    Scene.v().loadBasicClasses();
    JimpleConverter jimpleConverter = new JimpleConverter(Collections.singletonList(klass));
    soot.SootClass c = jimpleConverter.convertSootClass(klass);
    PrintWriter writer = new PrintWriter(System.out);
    soot.Printer.v().printTo(c, writer);
    // writer.flush();
    // writer.close();
  }

  @Ignore
  public void testSimple1() {
    declareClassSig = defaultFactories.getTypeFactory().getClassType("Simple1");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    klass = c.get();
  }

  @Ignore
  public void testVarDeclInSwitch() {
    declareClassSig = defaultFactories.getTypeFactory().getClassType("bugfixes.VarDeclInSwitch");
    Optional<SootClass> c = loader.getSootClass(declareClassSig);
    assertTrue(c.isPresent());
    klass = c.get();
    // Utils.print(klass, true);
  }
}
