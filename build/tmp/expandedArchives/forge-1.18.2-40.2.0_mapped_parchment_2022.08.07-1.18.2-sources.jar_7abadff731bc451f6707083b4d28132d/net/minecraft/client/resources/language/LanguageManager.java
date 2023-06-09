package net.minecraft.client.resources.language;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.stream.Stream;
import net.minecraft.client.resources.metadata.language.LanguageMetadataSection;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class LanguageManager implements ResourceManagerReloadListener {
   private static final Logger LOGGER = LogUtils.getLogger();
   public static final String DEFAULT_LANGUAGE_CODE = "en_us";
   private static final LanguageInfo DEFAULT_LANGUAGE = new LanguageInfo("en_us", "US", "English", false);
   private Map<String, LanguageInfo> languages = ImmutableMap.of("en_us", DEFAULT_LANGUAGE);
   private String currentCode;
   private LanguageInfo currentLanguage = DEFAULT_LANGUAGE;

   public LanguageManager(String pCurrentCode) {
      this.currentCode = pCurrentCode;
   }

   private static Map<String, LanguageInfo> extractLanguages(Stream<PackResources> pPackResources) {
      Map<String, LanguageInfo> map = Maps.newHashMap();
      pPackResources.forEach((p_118980_) -> {
         try {
            LanguageMetadataSection languagemetadatasection = p_118980_.getMetadataSection(LanguageMetadataSection.SERIALIZER);
            if (languagemetadatasection != null) {
               for(LanguageInfo languageinfo : languagemetadatasection.getLanguages()) {
                  map.putIfAbsent(languageinfo.getCode(), languageinfo);
               }
            }
         } catch (IOException | RuntimeException runtimeexception) {
            LOGGER.warn("Unable to parse language metadata section of resourcepack: {}", p_118980_.getName(), runtimeexception);
         }

      });
      return ImmutableMap.copyOf(map);
   }

   public void onResourceManagerReload(ResourceManager pResourceManager) {
      this.languages = extractLanguages(pResourceManager.listPacks());
      LanguageInfo languageinfo = this.languages.getOrDefault("en_us", DEFAULT_LANGUAGE);
      this.currentLanguage = this.languages.getOrDefault(this.currentCode, languageinfo);
      List<LanguageInfo> list = Lists.newArrayList(languageinfo);
      if (this.currentLanguage != languageinfo) {
         list.add(this.currentLanguage);
      }

      ClientLanguage clientlanguage = ClientLanguage.loadFrom(pResourceManager, list);
      I18n.setLanguage(clientlanguage);
      Language.inject(clientlanguage);
   }

   public void setSelected(LanguageInfo pCurrentLanguage) {
      this.currentCode = pCurrentLanguage.getCode();
      this.currentLanguage = pCurrentLanguage;
   }

   public LanguageInfo getSelected() {
      return this.currentLanguage;
   }

   public SortedSet<LanguageInfo> getLanguages() {
      return Sets.newTreeSet(this.languages.values());
   }

   public LanguageInfo getLanguage(String pCode) {
      return this.languages.get(pCode);
   }
}