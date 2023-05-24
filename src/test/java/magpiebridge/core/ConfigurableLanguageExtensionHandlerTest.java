package magpiebridge.core;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ConfigurableLanguageExtensionHandlerTest {

  @Test
  public void testEmptyValues() {
    ConfigurableLanguageExtensionHandler handler = ConfigurableLanguageExtensionHandler.empty();

    assertNull(handler.getLanguageForExtension(".c"));
    assertTrue(handler.getExtensionsForLanguage("c").isEmpty());
  }

  @Test
  public void testAddMapping() {
    ConfigurableLanguageExtensionHandler handler = ConfigurableLanguageExtensionHandler.empty();
    handler.setMapping("c", new String[]{".c", ".h"});

    assertEquals("c", handler.getLanguageForExtension(".c"));
    assertEquals("c", handler.getLanguageForExtension(".h"));
    assertEquals(ImmutableSet.of(".c", ".h"), handler.getExtensionsForLanguage("c"));
  }

  @Test
  public void testReplaceMapping() {
    ConfigurableLanguageExtensionHandler handler = ConfigurableLanguageExtensionHandler.empty();
    handler.setMapping("c", new String[]{".u", ".h", ".f"});
    handler.setMapping("c", new String[]{".c", ".h"});

    assertEquals("c", handler.getLanguageForExtension(".c"));
    assertEquals("c", handler.getLanguageForExtension(".h"));
    assertNull(handler.getLanguageForExtension(".f"));
    assertNull(handler.getLanguageForExtension(".u"));
    assertEquals(ImmutableSet.of(".c", ".h"), handler.getExtensionsForLanguage("c"));
  }

  @Test
  public void testPreservesOrder() {
    ConfigurableLanguageExtensionHandler handler = ConfigurableLanguageExtensionHandler.empty();
    handler.setMapping("c", new String[]{".c", ".d", ".g", ".a", ".b", ".f", ".a"});

    List<String> expected = Arrays.asList(".c", ".d", ".g", ".a", ".b", ".f");
    //noinspection SimplifyStreamApiCallChains
    assertEquals(expected, handler.getExtensionsForLanguage("c").stream().collect(Collectors.toList()));
    assertEquals(expected, new ArrayList<>(handler.getExtensionsForLanguage("c")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDisallowOverlapping() {
    ConfigurableLanguageExtensionHandler handler = ConfigurableLanguageExtensionHandler.empty();
    handler.setMapping("c++", new String[]{".cpp", ".h", ".hpp"});
    handler.setMapping("c", new String[]{".c", ".h"});
  }

}