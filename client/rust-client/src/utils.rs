

pub fn byte_to_gb(byte: f32) -> f32 {
    byte / 1024.0 / 1024.0 / 1024.0
}
pub fn byte_to_mb(byte: f32) -> f32 {
    byte / 1024.0 / 1024.0
}

pub fn round_f32(n: f32) -> f32 {
    (n * 100.0).round() / 100.0
}