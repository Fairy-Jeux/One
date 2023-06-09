package net.minecraft.server.packs.repository;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;

public class FolderRepositorySource implements RepositorySource {
   private static final FileFilter RESOURCEPACK_FILTER = (p_10398_) -> {
      boolean flag = p_10398_.isFile() && p_10398_.getName().endsWith(".zip");
      boolean flag1 = p_10398_.isDirectory() && (new File(p_10398_, "pack.mcmeta")).isFile();
      return flag || flag1;
   };
   private final File folder;
   private final PackSource packSource;

   public FolderRepositorySource(File p_10386_, PackSource p_10387_) {
      this.folder = p_10386_;
      this.packSource = p_10387_;
   }

   public void loadPacks(Consumer<Pack> pInfoConsumer, Pack.PackConstructor pInfoFactory) {
      if (!this.folder.isDirectory()) {
         this.folder.mkdirs();
      }

      File[] afile = this.folder.listFiles(RESOURCEPACK_FILTER);
      if (afile != null) {
         for(File file1 : afile) {
            String s = "file/" + file1.getName();
            Pack pack = Pack.create(s, false, this.createSupplier(file1), pInfoFactory, Pack.Position.TOP, this.packSource);
            if (pack != null) {
               pInfoConsumer.accept(pack);
            }
         }

      }
   }

   private Supplier<PackResources> createSupplier(File pFile) {
      return pFile.isDirectory() ? () -> {
         return new FolderPackResources(pFile);
      } : () -> {
         return new FilePackResources(pFile);
      };
   }
}