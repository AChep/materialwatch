#pragma version(1)
#pragma rs java_package_name(com.artemchep.essence.rs)
#pragma rs_fp_relaxed

rs_allocation inImage;
int inWidth;
int inHeight;

static float4 blend_argb(float4 a, float4 b, float ratio) {
    return (a * ratio) + (b * (1.0f - ratio));
}

uchar4 __attribute__ ((kernel)) arc_quart_clockwise (uchar4 in, uint32_t x, uint32_t y) {
    float radius = sqrt((float) (x * x + y * y)) + 0.0001f;
    float ratio = asin(x / radius) * M_2_PI;

    float inX = ratio * inWidth;
    float inY = radius;

    if (
        0 <= inX && inX <= inWidth - 1 &&
        0 <= inY && inY <= inHeight - 1
    ) {
        // Form the image basing on 4 points around
        // the computed point.
        float inX_l  = (uint32_t) inX;
        float inY_l = (uint32_t) inY;
        float inX_h  = inX_l + 1.0f;
        float inY_h = inY_l + 1.0f;

        float inX_ratio = (inX - inX_l) / 1.0f;
        float inY_ratio = (inY - inY_l) / 1.0f;

        // Get all the points.
        float4 in_ll = convert_float4(rsGetElementAt_uchar4(inImage, (uint32_t) inX_l, (uint32_t) inY_l));
        float4 in_lh = convert_float4(rsGetElementAt_uchar4(inImage, (uint32_t) inX_l, (uint32_t) inY_h));
        float4 in_hl = convert_float4(rsGetElementAt_uchar4(inImage, (uint32_t) inX_h, (uint32_t) inY_l));
        float4 in_hh = convert_float4(rsGetElementAt_uchar4(inImage, (uint32_t) inX_h, (uint32_t) inY_h));

        float4 in_ll_hl = blend_argb(in_hl, in_ll, inX_ratio);
        float4 in_lh_hh = blend_argb(in_hh, in_lh, inX_ratio);
        float4 in_out = blend_argb(in_lh_hh, in_ll_hl, inY_ratio);

        uchar4 out = in;
        out.a = (uint32_t) (in_out.w);
        out.r = (uint32_t) (in_out.x);
        out.g = (uint32_t) (in_out.y);
        out.b = (uint32_t) (in_out.z);
        return out;
    } else {
        const uchar4 *out = rsGetElementAt(inImage, 0, 0);
        return *out;
    }
}
