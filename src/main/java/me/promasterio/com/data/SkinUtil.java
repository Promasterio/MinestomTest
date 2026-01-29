package me.promasterio.com.data;

import net.minestom.server.entity.PlayerSkin;

import java.util.HashMap;
import java.util.Map;

public class SkinUtil {
    private static final Map<String, PlayerSkin> SKINS = new HashMap<>();

    public static void initializeSkins() {
        SKINS.put("devaster", new PlayerSkin("ewogICJ0aW1lc3RhbXAiIDogMTc2MDk4ODk2NzAxNCwKICAicHJvZmlsZUlkIiA6ICI3ZGY4NmY1MWFjZmI0MjQzYTkzNDQ1OTAyZDEzYTc0MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJNclJpcHRpZGUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWIxZjJjNWExNWQ2M2YxMzY5MmM5NTdlMTNkOGNhOTNmZGY3NGJmZTBiNDRmODgyYzcxMGYzMDZkYTc0MDQ1NiIKICAgIH0KICB9Cn0=", "EvfxWjdPqwS/mxOfGR2H1BxqEx4mEy3zrAnbLB5vb/QRI8U7Boq4udibOq0HV2dpfJat7DQmrLnsEykTiQPmNGeQmDYI2ZndZZu/cWKeNp0DA++Xke17NPGWLkUsIcmn6xnQS7Phj99exyMzHglRNXwf9mTz0MXrOmeflwW2hDOYFxX/KAelRQ0zob/2MN+LP2orToAljhLNkypT505U8RsOtbcED8DjQP3y0/E5SgYYYAxWJXjOCmYLTlF9bJhv42hDUWPLq1JpieFWxKfICSTlvlZzBAdYr54zA2DuGUehtseev0OdBIDCki7Eqt5XxmPgxainIFpSUsxTFm5feNbXuqj1iHDeV66bBNVUODQHKpqtj8smnlrqdB692xtx2E89NRFQKdp4KyJ6L/QgizzJq6tymLU9DAbA7sfztDeoJtDgg/R4w7Cj/G42GNJzEkrieMN7JNJ+OXDsedMKXyvXrteTvIzYWvbXuwtcYFL7icz6Bn2GZaCrtB3TElqIkiBRkUN3mrV01x6UkJQ7MfRwg2NYC89iiD+xpgGg7ppWcISy+dCxOkN3FvB/37y2n7HRs9yWGcDwB6PLIhdtb5IuDRRaY29OcmsGFVSMi2Tt0Td3rxltaxTqcfxRz5aVm2666e7+0dQpHaU4kAK/DrS3maWEzNL82FemivdXMhI="));
    }
    public static PlayerSkin getSkin(String name) {
        return SKINS.get(name);
    }
}
