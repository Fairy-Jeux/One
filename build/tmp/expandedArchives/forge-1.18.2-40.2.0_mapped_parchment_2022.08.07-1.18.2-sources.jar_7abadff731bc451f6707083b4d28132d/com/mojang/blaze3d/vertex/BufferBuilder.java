package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class BufferBuilder extends DefaultedVertexConsumer implements BufferVertexConsumer {
   private static final int GROWTH_SIZE = 2097152;
   private static final Logger LOGGER = LogUtils.getLogger();
   private ByteBuffer buffer;
   private final List<BufferBuilder.DrawState> drawStates = Lists.newArrayList();
   private int lastPoppedStateIndex;
   private int totalRenderedBytes;
   private int nextElementByte;
   private int totalUploadedBytes;
   private int vertices;
   @Nullable
   private VertexFormatElement currentElement;
   private int elementIndex;
   private VertexFormat format;
   private VertexFormat.Mode mode;
   private boolean fastFormat;
   private boolean fullFormat;
   private boolean building;
   @Nullable
   private Vector3f[] sortingPoints;
   private float sortX = Float.NaN;
   private float sortY = Float.NaN;
   private float sortZ = Float.NaN;
   private boolean indexOnly;

   public BufferBuilder(int pCapacity) {
      this.buffer = MemoryTracker.create(pCapacity * 6);
   }

   private void ensureVertexCapacity() {
      this.ensureCapacity(this.format.getVertexSize());
   }

   private void ensureCapacity(int pIncreaseAmount) {
      if (this.nextElementByte + pIncreaseAmount > this.buffer.capacity()) {
         int i = this.buffer.capacity();
         int j = i + roundUp(pIncreaseAmount);
         LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", i, j);
         ByteBuffer bytebuffer = MemoryTracker.resize(this.buffer, j);
         bytebuffer.rewind();
         this.buffer = bytebuffer;
      }
   }

   private static int roundUp(int pX) {
      int i = 2097152;
      if (pX == 0) {
         return i;
      } else {
         if (pX < 0) {
            i *= -1;
         }

         int j = pX % i;
         return j == 0 ? pX : pX + i - j;
      }
   }

   public void setQuadSortOrigin(float pSortX, float pSortY, float pSortZ) {
      if (this.mode == VertexFormat.Mode.QUADS) {
         if (this.sortX != pSortX || this.sortY != pSortY || this.sortZ != pSortZ) {
            this.sortX = pSortX;
            this.sortY = pSortY;
            this.sortZ = pSortZ;
            if (this.sortingPoints == null) {
               this.sortingPoints = this.makeQuadSortingPoints();
            }
         }

      }
   }

   public BufferBuilder.SortState getSortState() {
      return new BufferBuilder.SortState(this.mode, this.vertices, this.sortingPoints, this.sortX, this.sortY, this.sortZ);
   }

   public void restoreSortState(BufferBuilder.SortState pSortState) {
      this.buffer.clear();
      this.mode = pSortState.mode;
      this.vertices = pSortState.vertices;
      this.nextElementByte = this.totalRenderedBytes;
      this.sortingPoints = pSortState.sortingPoints;
      this.sortX = pSortState.sortX;
      this.sortY = pSortState.sortY;
      this.sortZ = pSortState.sortZ;
      this.indexOnly = true;
   }

   public void begin(VertexFormat.Mode pMode, VertexFormat pFormat) {
      if (this.building) {
         throw new IllegalStateException("Already building!");
      } else {
         this.building = true;
         this.mode = pMode;
         this.switchFormat(pFormat);
         this.currentElement = pFormat.getElements().get(0);
         this.elementIndex = 0;
         this.buffer.clear();
      }
   }

   private void switchFormat(VertexFormat pFormat) {
      if (this.format != pFormat) {
         this.format = pFormat;
         boolean flag = pFormat == DefaultVertexFormat.NEW_ENTITY;
         boolean flag1 = pFormat == DefaultVertexFormat.BLOCK;
         this.fastFormat = flag || flag1;
         this.fullFormat = flag;
      }
   }

   private IntConsumer intConsumer(VertexFormat.IndexType pIndexType) {
      switch(pIndexType) {
      case BYTE:
         return (p_166793_) -> {
            this.buffer.put((byte)p_166793_);
         };
      case SHORT:
         return (p_166791_) -> {
            this.buffer.putShort((short)p_166791_);
         };
      case INT:
      default:
         return (p_166789_) -> {
            this.buffer.putInt(p_166789_);
         };
      }
   }

   private Vector3f[] makeQuadSortingPoints() {
      FloatBuffer floatbuffer = this.buffer.asFloatBuffer();
      int i = this.totalRenderedBytes / 4;
      int j = this.format.getIntegerSize();
      int k = j * this.mode.primitiveStride;
      int l = this.vertices / this.mode.primitiveStride;
      Vector3f[] avector3f = new Vector3f[l];

      for(int i1 = 0; i1 < l; ++i1) {
         float f = floatbuffer.get(i + i1 * k + 0);
         float f1 = floatbuffer.get(i + i1 * k + 1);
         float f2 = floatbuffer.get(i + i1 * k + 2);
         float f3 = floatbuffer.get(i + i1 * k + j * 2 + 0);
         float f4 = floatbuffer.get(i + i1 * k + j * 2 + 1);
         float f5 = floatbuffer.get(i + i1 * k + j * 2 + 2);
         float f6 = (f + f3) / 2.0F;
         float f7 = (f1 + f4) / 2.0F;
         float f8 = (f2 + f5) / 2.0F;
         avector3f[i1] = new Vector3f(f6, f7, f8);
      }

      return avector3f;
   }

   private void putSortedQuadIndices(VertexFormat.IndexType pIndexType) {
      float[] afloat = new float[this.sortingPoints.length];
      int[] aint = new int[this.sortingPoints.length];

      for(int i = 0; i < this.sortingPoints.length; aint[i] = i++) {
         float f = this.sortingPoints[i].x() - this.sortX;
         float f1 = this.sortingPoints[i].y() - this.sortY;
         float f2 = this.sortingPoints[i].z() - this.sortZ;
         afloat[i] = f * f + f1 * f1 + f2 * f2;
      }

      IntArrays.mergeSort(aint, (p_166784_, p_166785_) -> {
         return Floats.compare(afloat[p_166785_], afloat[p_166784_]);
      });
      IntConsumer intconsumer = this.intConsumer(pIndexType);
      this.buffer.position(this.nextElementByte);

      for(int j : aint) {
         intconsumer.accept(j * this.mode.primitiveStride + 0);
         intconsumer.accept(j * this.mode.primitiveStride + 1);
         intconsumer.accept(j * this.mode.primitiveStride + 2);
         intconsumer.accept(j * this.mode.primitiveStride + 2);
         intconsumer.accept(j * this.mode.primitiveStride + 3);
         intconsumer.accept(j * this.mode.primitiveStride + 0);
      }

   }

   public void end() {
      if (!this.building) {
         throw new IllegalStateException("Not building!");
      } else {
         int i = this.mode.indexCount(this.vertices);
         VertexFormat.IndexType vertexformat$indextype = VertexFormat.IndexType.least(i);
         boolean flag;
         if (this.sortingPoints != null) {
            int j = Mth.roundToward(i * vertexformat$indextype.bytes, 4);
            this.ensureCapacity(j);
            this.putSortedQuadIndices(vertexformat$indextype);
            flag = false;
            this.nextElementByte += j;
            this.totalRenderedBytes += this.vertices * this.format.getVertexSize() + j;
         } else {
            flag = true;
            this.totalRenderedBytes += this.vertices * this.format.getVertexSize();
         }

         this.building = false;
         this.drawStates.add(new BufferBuilder.DrawState(this.format, this.vertices, i, this.mode, vertexformat$indextype, this.indexOnly, flag));
         this.vertices = 0;
         this.currentElement = null;
         this.elementIndex = 0;
         this.sortingPoints = null;
         this.sortX = Float.NaN;
         this.sortY = Float.NaN;
         this.sortZ = Float.NaN;
         this.indexOnly = false;
      }
   }

   public void putByte(int pIndex, byte pByteValue) {
      this.buffer.put(this.nextElementByte + pIndex, pByteValue);
   }

   public void putShort(int pIndex, short pShortValue) {
      this.buffer.putShort(this.nextElementByte + pIndex, pShortValue);
   }

   public void putFloat(int pIndex, float pFloatValue) {
      this.buffer.putFloat(this.nextElementByte + pIndex, pFloatValue);
   }

   public void endVertex() {
      if (this.elementIndex != 0) {
         throw new IllegalStateException("Not filled all elements of the vertex");
      } else {
         ++this.vertices;
         this.ensureVertexCapacity();
         if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
            int i = this.format.getVertexSize();
            this.buffer.position(this.nextElementByte);
            ByteBuffer bytebuffer = this.buffer.duplicate();
            bytebuffer.position(this.nextElementByte - i).limit(this.nextElementByte);
            this.buffer.put(bytebuffer);
            this.nextElementByte += i;
            ++this.vertices;
            this.ensureVertexCapacity();
         }

      }
   }

   public void nextElement() {
      ImmutableList<VertexFormatElement> immutablelist = this.format.getElements();
      this.elementIndex = (this.elementIndex + 1) % immutablelist.size();
      this.nextElementByte += this.currentElement.getByteSize();
      VertexFormatElement vertexformatelement = immutablelist.get(this.elementIndex);
      this.currentElement = vertexformatelement;
      if (vertexformatelement.getUsage() == VertexFormatElement.Usage.PADDING) {
         this.nextElement();
      }

      if (this.defaultColorSet && this.currentElement.getUsage() == VertexFormatElement.Usage.COLOR) {
         BufferVertexConsumer.super.color(this.defaultR, this.defaultG, this.defaultB, this.defaultA);
      }

   }

   public VertexConsumer color(int pRed, int pGreen, int pBlue, int pAlpha) {
      if (this.defaultColorSet) {
         throw new IllegalStateException();
      } else {
         return BufferVertexConsumer.super.color(pRed, pGreen, pBlue, pAlpha);
      }
   }

   public void vertex(float pX, float pY, float pZ, float pRed, float pGreen, float pBlue, float pAlpha, float pTexU, float pTexV, int pOverlayUV, int pLightmapUV, float pNormalX, float pNormalY, float pNormalZ) {
      if (this.defaultColorSet) {
         throw new IllegalStateException();
      } else if (this.fastFormat) {
         this.putFloat(0, pX);
         this.putFloat(4, pY);
         this.putFloat(8, pZ);
         this.putByte(12, (byte)((int)(pRed * 255.0F)));
         this.putByte(13, (byte)((int)(pGreen * 255.0F)));
         this.putByte(14, (byte)((int)(pBlue * 255.0F)));
         this.putByte(15, (byte)((int)(pAlpha * 255.0F)));
         this.putFloat(16, pTexU);
         this.putFloat(20, pTexV);
         int i;
         if (this.fullFormat) {
            this.putShort(24, (short)(pOverlayUV & '\uffff'));
            this.putShort(26, (short)(pOverlayUV >> 16 & '\uffff'));
            i = 28;
         } else {
            i = 24;
         }

         this.putShort(i + 0, (short)(pLightmapUV & '\uffff'));
         this.putShort(i + 2, (short)(pLightmapUV >> 16 & '\uffff'));
         this.putByte(i + 4, BufferVertexConsumer.normalIntValue(pNormalX));
         this.putByte(i + 5, BufferVertexConsumer.normalIntValue(pNormalY));
         this.putByte(i + 6, BufferVertexConsumer.normalIntValue(pNormalZ));
         this.nextElementByte += i + 8;
         this.endVertex();
      } else {
         super.vertex(pX, pY, pZ, pRed, pGreen, pBlue, pAlpha, pTexU, pTexV, pOverlayUV, pLightmapUV, pNormalX, pNormalY, pNormalZ);
      }
   }

   public Pair<BufferBuilder.DrawState, ByteBuffer> popNextBuffer() {
      BufferBuilder.DrawState bufferbuilder$drawstate = this.drawStates.get(this.lastPoppedStateIndex++);
      this.buffer.position(this.totalUploadedBytes);
      this.totalUploadedBytes += Mth.roundToward(bufferbuilder$drawstate.bufferSize(), 4);
      this.buffer.limit(this.totalUploadedBytes);
      if (this.lastPoppedStateIndex == this.drawStates.size() && this.vertices == 0) {
         this.clear();
      }

      ByteBuffer bytebuffer = this.buffer.slice();
      bytebuffer.order(this.buffer.order()); // FORGE: Fix incorrect byte order
      this.buffer.clear();
      return Pair.of(bufferbuilder$drawstate, bytebuffer);
   }

   public void clear() {
      if (this.totalRenderedBytes != this.totalUploadedBytes) {
         LOGGER.warn("Bytes mismatch {} {}", this.totalRenderedBytes, this.totalUploadedBytes);
      }

      this.discard();
   }

   public void discard() {
      this.totalRenderedBytes = 0;
      this.totalUploadedBytes = 0;
      this.nextElementByte = 0;
      this.drawStates.clear();
      this.lastPoppedStateIndex = 0;
   }

   public VertexFormatElement currentElement() {
      if (this.currentElement == null) {
         throw new IllegalStateException("BufferBuilder not started");
      } else {
         return this.currentElement;
      }
   }

   public boolean building() {
      return this.building;
   }

   @OnlyIn(Dist.CLIENT)
   public static final class DrawState {
      private final VertexFormat format;
      private final int vertexCount;
      private final int indexCount;
      private final VertexFormat.Mode mode;
      private final VertexFormat.IndexType indexType;
      private final boolean indexOnly;
      private final boolean sequentialIndex;

      DrawState(VertexFormat pFormat, int pVertexCount, int pIndexCount, VertexFormat.Mode pMode, VertexFormat.IndexType pIndexType, boolean pIndexOnly, boolean pSequentialIndex) {
         this.format = pFormat;
         this.vertexCount = pVertexCount;
         this.indexCount = pIndexCount;
         this.mode = pMode;
         this.indexType = pIndexType;
         this.indexOnly = pIndexOnly;
         this.sequentialIndex = pSequentialIndex;
      }

      public VertexFormat format() {
         return this.format;
      }

      public int vertexCount() {
         return this.vertexCount;
      }

      public int indexCount() {
         return this.indexCount;
      }

      public VertexFormat.Mode mode() {
         return this.mode;
      }

      public VertexFormat.IndexType indexType() {
         return this.indexType;
      }

      public int vertexBufferSize() {
         return this.vertexCount * this.format.getVertexSize();
      }

      private int indexBufferSize() {
         return this.sequentialIndex ? 0 : this.indexCount * this.indexType.bytes;
      }

      public int bufferSize() {
         return this.vertexBufferSize() + this.indexBufferSize();
      }

      public boolean indexOnly() {
         return this.indexOnly;
      }

      public boolean sequentialIndex() {
         return this.sequentialIndex;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public static class SortState {
      final VertexFormat.Mode mode;
      final int vertices;
      @Nullable
      final Vector3f[] sortingPoints;
      final float sortX;
      final float sortY;
      final float sortZ;

      SortState(VertexFormat.Mode pMode, int pVertices, @Nullable Vector3f[] pSortingPoints, float pSortX, float pSortY, float pSortZ) {
         this.mode = pMode;
         this.vertices = pVertices;
         this.sortingPoints = pSortingPoints;
         this.sortX = pSortX;
         this.sortY = pSortY;
         this.sortZ = pSortZ;
      }
   }

   // Forge start
   public void putBulkData(ByteBuffer buffer) {
      ensureCapacity(buffer.limit() + this.format.getVertexSize());
      this.buffer.position(this.vertices * this.format.getVertexSize());
      this.buffer.put(buffer);
      this.vertices += buffer.limit() / this.format.getVertexSize();
      this.nextElementByte += buffer.limit();
   }

   @Override
   public VertexFormat getVertexFormat() { return this.format; }
}
