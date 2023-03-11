package net.minecraft.client.resources;

import com.google.common.collect.ImmutableSet;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.util.ResourceLocation;

public class DefaultResourcePack implements IResourcePack {
	public static final Set<String> field_110608_a = ImmutableSet.<String>of("minecraft", "realms"/* WDL >>> */, "wdl" /* <<< WDL */);
	private final ResourceIndex field_188549_b;

	public DefaultResourcePack(ResourceIndex p_i46541_1_) {
		this.field_188549_b = p_i46541_1_;
	}

	public InputStream func_110590_a(ResourceLocation p_110590_1_) throws IOException {
		InputStream inputstream = this.func_110605_c(p_110590_1_);

		if (inputstream != null) {
			return inputstream;
		} else {
			InputStream inputstream1 = this.func_152780_c(p_110590_1_);

			if (inputstream1 != null) {
				return inputstream1;
			} else {
				throw new FileNotFoundException(p_110590_1_.getPath());
			}
		}
	}

	@Nullable
	public InputStream func_152780_c(ResourceLocation p_152780_1_) throws IOException, FileNotFoundException {
		File file1 = this.field_188549_b.getFile(p_152780_1_);
		return file1 != null && file1.isFile() ? new FileInputStream(file1) : null;
	}

	private InputStream func_110605_c(ResourceLocation p_110605_1_) {
		return DefaultResourcePack.class.getResourceAsStream("/assets/" + p_110605_1_.getNamespace() + "/" + p_110605_1_.getPath());
	}

	public boolean func_110589_b(ResourceLocation p_110589_1_) {
		return this.func_110605_c(p_110589_1_) != null || this.field_188549_b.func_188545_b(p_110589_1_);
	}

	public Set<String> getResourceDomains() {
		return field_110608_a;
	}

	/**
	 * Name required by liteloader
	 */
	public <T extends IMetadataSection> T getPackMetadata(MetadataSerializer p_135058_1_, String p_135058_2_) throws IOException {
		try {
			InputStream inputstream = new FileInputStream(this.field_188549_b.func_188546_a());
			return AbstractResourcePack.func_110596_a(p_135058_1_, inputstream, p_135058_2_);
		} catch (RuntimeException var4) {
			return (T)null;
		} catch (FileNotFoundException var5) {
			return (T)null;
		}
	}

	public BufferedImage func_110586_a() throws IOException {
		return TextureUtil.func_177053_a(DefaultResourcePack.class.getResourceAsStream("/" + (new ResourceLocation("pack.png")).getPath()));
	}

	public String func_130077_b() {
		return "Default";
	}
}
