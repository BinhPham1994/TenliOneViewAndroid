package com.tenli.oneview.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Exact 24x24 stroke-outline icons from Lucide Icons (matching lucide-vue-next in Tenli OneView PWA).
 */
object LucideIcons {

    private fun lucideVector(
        name: String,
        strokeWidth: Float = 2f,
        builder: ImageVector.Builder.() -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(builder).build()
    }

    val Home: ImageVector by lazy {
        lucideVector("LucideHome") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8
                moveTo(15f, 21f)
                verticalLineTo(13f)
                arcTo(1f, 1f, 0f, false, false, 14f, 12f)
                horizontalLineTo(10f)
                arcTo(1f, 1f, 0f, false, false, 9f, 13f)
                verticalLineTo(21f)
                // M3 10a2 2 0 0 1 .709-1.528l7-5.999a2 2 0 0 1 2.582 0l7 5.999A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z
                moveTo(3f, 10f)
                arcTo(2f, 2f, 0f, false, true, 3.709f, 8.472f)
                lineTo(10.709f, 2.473f)
                arcTo(2f, 2f, 0f, false, true, 13.291f, 2.473f)
                lineTo(20.291f, 8.472f)
                arcTo(2f, 2f, 0f, false, true, 21f, 10f)
                verticalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 19f, 21f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 3f, 19f)
                close()
            }
        }
    }

    val Bell: ImageVector by lazy {
        lucideVector("LucideBell") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M10.268 21a2 2 0 0 0 3.464 0
                moveTo(10.268f, 21f)
                arcTo(2f, 2f, 0f, false, false, 13.732f, 21f)
                // M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326
                moveTo(3.262f, 15.326f)
                arcTo(1f, 1f, 0f, false, false, 4f, 17f)
                horizontalLineTo(20f)
                arcTo(1f, 1f, 0f, false, false, 20.74f, 15.327f)
                curveTo(19.41f, 13.956f, 18f, 12.499f, 18f, 8f)
                arcTo(6f, 6f, 0f, false, false, 6f, 8f)
                curveTo(6f, 12.499f, 4.589f, 13.956f, 3.262f, 15.326f)
                close()
            }
        }
    }

    val Video: ImageVector by lazy {
        lucideVector("LucideVideo") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m16 13 5.223 3.482a.5.5 0 0 0 .777-.416V7.87a.5.5 0 0 0-.752-.432L16 10.5
                moveTo(16f, 13f)
                lineTo(21.223f, 16.482f)
                arcTo(0.5f, 0.5f, 0f, false, false, 22f, 16.066f)
                verticalLineTo(7.87f)
                arcTo(0.5f, 0.5f, 0f, false, false, 21.248f, 7.438f)
                lineTo(16f, 10.5f)
                // rect x="2" y="6" width="14" height="12" rx="2"
                moveTo(4f, 6f)
                horizontalLineTo(14f)
                arcTo(2f, 2f, 0f, false, true, 16f, 8f)
                verticalLineTo(16f)
                arcTo(2f, 2f, 0f, false, true, 14f, 18f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 16f)
                verticalLineTo(8f)
                arcTo(2f, 2f, 0f, false, true, 4f, 6f)
                close()
            }
        }
    }

    val Settings: ImageVector by lazy {
        lucideVector("LucideSettings") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z
                moveTo(12.22f, 2f)
                horizontalLineTo(11.78f)
                arcTo(2f, 2f, 0f, false, false, 9.78f, 4f)
                verticalLineTo(4.18f)
                arcTo(2f, 2f, 0f, false, true, 8.78f, 5.91f)
                lineTo(8.35f, 6.16f)
                arcTo(2f, 2f, 0f, false, true, 6.35f, 6.16f)
                lineTo(6.2f, 6.08f)
                arcTo(2f, 2f, 0f, false, false, 3.47f, 6.81f)
                lineTo(3.25f, 7.19f)
                arcTo(2f, 2f, 0f, false, false, 3.98f, 9.92f)
                lineTo(4.13f, 10.02f)
                arcTo(2f, 2f, 0f, false, true, 5.13f, 11.74f)
                verticalLineTo(12.25f)
                arcTo(2f, 2f, 0f, false, true, 4.13f, 13.99f)
                lineTo(3.98f, 14.08f)
                arcTo(2f, 2f, 0f, false, false, 3.25f, 16.81f)
                lineTo(3.47f, 17.19f)
                arcTo(2f, 2f, 0f, false, false, 6.2f, 17.92f)
                lineTo(6.35f, 17.84f)
                arcTo(2f, 2f, 0f, false, true, 8.35f, 17.84f)
                lineTo(8.78f, 18.09f)
                arcTo(2f, 2f, 0f, false, true, 9.78f, 19.82f)
                verticalLineTo(20f)
                arcTo(2f, 2f, 0f, false, false, 11.78f, 22f)
                horizontalLineTo(12.22f)
                arcTo(2f, 2f, 0f, false, false, 14.22f, 20f)
                verticalLineTo(19.82f)
                arcTo(2f, 2f, 0f, false, true, 15.22f, 18.09f)
                lineTo(15.65f, 17.84f)
                arcTo(2f, 2f, 0f, false, true, 17.65f, 17.84f)
                lineTo(17.8f, 17.92f)
                arcTo(2f, 2f, 0f, false, false, 20.53f, 17.19f)
                lineTo(20.75f, 16.8f)
                arcTo(2f, 2f, 0f, false, false, 20.02f, 14.07f)
                lineTo(19.87f, 13.99f)
                arcTo(2f, 2f, 0f, false, true, 18.87f, 12.25f)
                verticalLineTo(11.75f)
                arcTo(2f, 2f, 0f, false, true, 19.87f, 10.01f)
                lineTo(20.02f, 9.92f)
                arcTo(2f, 2f, 0f, false, false, 20.75f, 7.19f)
                lineTo(20.53f, 6.81f)
                arcTo(2f, 2f, 0f, false, false, 17.8f, 6.08f)
                lineTo(17.65f, 6.16f)
                arcTo(2f, 2f, 0f, false, true, 15.65f, 6.16f)
                lineTo(15.22f, 5.91f)
                arcTo(2f, 2f, 0f, false, true, 14.22f, 4.18f)
                verticalLineTo(4f)
                arcTo(2f, 2f, 0f, false, false, 12.22f, 2f)
                close()
                // circle cx="12" cy="12" r="3"
                moveTo(15f, 12f)
                arcTo(3f, 3f, 0f, true, true, 9f, 12f)
                arcTo(3f, 3f, 0f, true, true, 15f, 12f)
                close()
            }
        }
    }

    val Shield: ImageVector by lazy {
        lucideVector("LucideShield") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z
                moveTo(20f, 13f)
                curveTo(20f, 18f, 16.5f, 20.5f, 12.34f, 21.95f)
                arcTo(1f, 1f, 0f, false, true, 11.67f, 21.94f)
                curveTo(7.5f, 20.5f, 4f, 18f, 4f, 13f)
                verticalLineTo(6f)
                arcTo(1f, 1f, 0f, false, true, 5f, 5f)
                curveTo(7f, 5f, 9.5f, 3.8f, 11.24f, 2.28f)
                arcTo(1.17f, 1.17f, 0f, false, true, 12.76f, 2.28f)
                curveTo(14.51f, 3.81f, 17f, 5f, 19f, 5f)
                arcTo(1f, 1f, 0f, false, true, 20f, 6f)
                close()
            }
        }
    }

    val Disc: ImageVector by lazy {
        lucideVector("LucideDisc") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="12" cy="12" r="10"
                moveTo(22f, 12f)
                arcTo(10f, 10f, 0f, true, true, 2f, 12f)
                arcTo(10f, 10f, 0f, true, true, 22f, 12f)
                close()
                // circle cx="12" cy="12" r="2"
                moveTo(14f, 12f)
                arcTo(2f, 2f, 0f, true, true, 10f, 12f)
                arcTo(2f, 2f, 0f, true, true, 14f, 12f)
                close()
            }
        }
    }

    val ListFilter: ImageVector by lazy {
        lucideVector("LucideListFilter") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M3 6h18
                moveTo(3f, 6f)
                horizontalLineTo(21f)
                // M7 12h10
                moveTo(7f, 12f)
                horizontalLineTo(17f)
                // M10 18h4
                moveTo(10f, 18f)
                horizontalLineTo(14f)
            }
        }
    }

    val LayoutGrid: ImageVector by lazy {
        lucideVector("LucideLayoutGrid") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // rect width="7" height="7" x="3" y="3" rx="1"
                moveTo(4f, 3f)
                horizontalLineTo(9f)
                arcTo(1f, 1f, 0f, false, true, 10f, 4f)
                verticalLineTo(9f)
                arcTo(1f, 1f, 0f, false, true, 9f, 10f)
                horizontalLineTo(4f)
                arcTo(1f, 1f, 0f, false, true, 3f, 9f)
                verticalLineTo(4f)
                arcTo(1f, 1f, 0f, false, true, 4f, 3f)
                close()
                // rect width="7" height="7" x="14" y="3" rx="1"
                moveTo(15f, 3f)
                horizontalLineTo(20f)
                arcTo(1f, 1f, 0f, false, true, 21f, 4f)
                verticalLineTo(9f)
                arcTo(1f, 1f, 0f, false, true, 20f, 10f)
                horizontalLineTo(15f)
                arcTo(1f, 1f, 0f, false, true, 14f, 9f)
                verticalLineTo(4f)
                arcTo(1f, 1f, 0f, false, true, 15f, 3f)
                close()
                // rect width="7" height="7" x="14" y="14" rx="1"
                moveTo(15f, 14f)
                horizontalLineTo(20f)
                arcTo(1f, 1f, 0f, false, true, 21f, 15f)
                verticalLineTo(20f)
                arcTo(1f, 1f, 0f, false, true, 20f, 21f)
                horizontalLineTo(15f)
                arcTo(1f, 1f, 0f, false, true, 14f, 20f)
                verticalLineTo(15f)
                arcTo(1f, 1f, 0f, false, true, 15f, 14f)
                close()
                // rect width="7" height="7" x="3" y="14" rx="1"
                moveTo(4f, 14f)
                horizontalLineTo(9f)
                arcTo(1f, 1f, 0f, false, true, 10f, 15f)
                verticalLineTo(20f)
                arcTo(1f, 1f, 0f, false, true, 9f, 21f)
                horizontalLineTo(4f)
                arcTo(1f, 1f, 0f, false, true, 3f, 20f)
                verticalLineTo(15f)
                arcTo(1f, 1f, 0f, false, true, 4f, 14f)
                close()
            }
        }
    }

    val List: ImageVector by lazy {
        lucideVector("LucideList") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M3 12h.01 M3 18h.01 M3 6h.01 M8 12h13 M8 18h13 M8 6h13
                moveTo(3f, 6f)
                horizontalLineTo(3.01f)
                moveTo(3f, 12f)
                horizontalLineTo(3.01f)
                moveTo(3f, 18f)
                horizontalLineTo(3.01f)
                moveTo(8f, 6f)
                horizontalLineTo(21f)
                moveTo(8f, 12f)
                horizontalLineTo(21f)
                moveTo(8f, 18f)
                horizontalLineTo(21f)
            }
        }
    }

    val User: ImageVector by lazy {
        lucideVector("LucideUser") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2
                moveTo(19f, 21f)
                verticalLineTo(19f)
                arcTo(4f, 4f, 0f, false, false, 15f, 15f)
                horizontalLineTo(9f)
                arcTo(4f, 4f, 0f, false, false, 5f, 19f)
                verticalLineTo(21f)
                // circle cx="12" cy="7" r="4"
                moveTo(16f, 7f)
                arcTo(4f, 4f, 0f, true, true, 8f, 7f)
                arcTo(4f, 4f, 0f, true, true, 16f, 7f)
                close()
            }
        }
    }

    val Info: ImageVector by lazy {
        lucideVector("LucideInfo") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="12" cy="12" r="10"
                moveTo(22f, 12f)
                arcTo(10f, 10f, 0f, true, true, 2f, 12f)
                arcTo(10f, 10f, 0f, true, true, 22f, 12f)
                close()
                // M12 16v-4
                moveTo(12f, 16f)
                verticalLineTo(12f)
                // M12 8h.01
                moveTo(12f, 8f)
                horizontalLineTo(12.01f)
            }
        }
    }

    val Globe: ImageVector by lazy {
        lucideVector("LucideGlobe") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="12" cy="12" r="10"
                moveTo(22f, 12f)
                arcTo(10f, 10f, 0f, true, true, 2f, 12f)
                arcTo(10f, 10f, 0f, true, true, 22f, 12f)
                close()
                // M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20
                moveTo(12f, 2f)
                arcTo(14.5f, 14.5f, 0f, false, false, 12f, 22f)
                arcTo(14.5f, 14.5f, 0f, false, false, 12f, 2f)
                // M2 12h20
                moveTo(2f, 12f)
                horizontalLineTo(22f)
            }
        }
    }

    val Palette: ImageVector by lazy {
        lucideVector("LucidePalette") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10c.926 0 1.648-.746 1.648-1.688 0-.437-.18-.835-.437-1.125-.29-.289-.438-.652-.438-1.125a1.64 1.64 0 0 1 1.668-1.668h1.996c3.051 0 5.555-2.503 5.555-5.554C21.965 6.012 17.461 2 12 2z
                moveTo(12f, 2f)
                curveTo(6.5f, 2f, 2f, 6.5f, 2f, 12f)
                curveTo(2f, 17.5f, 6.5f, 22f, 12f, 22f)
                curveTo(12.926f, 22f, 13.648f, 21.254f, 13.648f, 20.312f)
                curveTo(13.648f, 19.875f, 13.468f, 19.477f, 13.211f, 19.187f)
                curveTo(12.921f, 18.898f, 12.773f, 18.535f, 12.773f, 18.062f)
                arcTo(1.64f, 1.64f, 0f, false, true, 14.441f, 16.394f)
                horizontalLineTo(16.437f)
                curveTo(19.488f, 16.394f, 21.992f, 13.891f, 21.992f, 10.84f)
                curveTo(21.965f, 6.012f, 17.461f, 2f, 12f, 2f)
                close()
                // circles at (13.5, 6.5), (17.5, 10.5), (8.5, 7.5), (6.5, 12.5)
                moveTo(14f, 6.5f)
                arcTo(0.5f, 0.5f, 0f, true, true, 13f, 6.5f)
                arcTo(0.5f, 0.5f, 0f, true, true, 14f, 6.5f)
                moveTo(18f, 10.5f)
                arcTo(0.5f, 0.5f, 0f, true, true, 17f, 10.5f)
                arcTo(0.5f, 0.5f, 0f, true, true, 18f, 10.5f)
                moveTo(9f, 7.5f)
                arcTo(0.5f, 0.5f, 0f, true, true, 8f, 7.5f)
                arcTo(0.5f, 0.5f, 0f, true, true, 9f, 7.5f)
                moveTo(7f, 12.5f)
                arcTo(0.5f, 0.5f, 0f, true, true, 6f, 12.5f)
                arcTo(0.5f, 0.5f, 0f, true, true, 7f, 12.5f)
            }
        }
    }

    val Lock: ImageVector by lazy {
        lucideVector("LucideLock") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // rect width="18" height="11" x="3" y="11" rx="2" ry="2"
                moveTo(5f, 11f)
                horizontalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 21f, 13f)
                verticalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 19f, 22f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 3f, 20f)
                verticalLineTo(13f)
                arcTo(2f, 2f, 0f, false, true, 5f, 11f)
                close()
                // M7 11V7a5 5 0 0 1 10 0v4
                moveTo(7f, 11f)
                verticalLineTo(7f)
                arcTo(5f, 5f, 0f, false, true, 17f, 7f)
                verticalLineTo(11f)
            }
        }
    }

    val ShieldAlert: ImageVector by lazy {
        lucideVector("LucideShieldAlert") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20f, 13f)
                curveTo(20f, 18f, 16.5f, 20.5f, 12.34f, 21.95f)
                arcTo(1f, 1f, 0f, false, true, 11.67f, 21.94f)
                curveTo(7.5f, 20.5f, 4f, 18f, 4f, 13f)
                verticalLineTo(6f)
                arcTo(1f, 1f, 0f, false, true, 5f, 5f)
                curveTo(7f, 5f, 9.5f, 3.8f, 11.24f, 2.28f)
                arcTo(1.17f, 1.17f, 0f, false, true, 12.76f, 2.28f)
                curveTo(14.51f, 3.81f, 17f, 5f, 19f, 5f)
                arcTo(1f, 1f, 0f, false, true, 20f, 6f)
                close()
                moveTo(12f, 8f)
                verticalLineTo(12f)
                moveTo(12f, 16f)
                horizontalLineTo(12.01f)
            }
        }
    }

    val CheckCircle: ImageVector by lazy {
        lucideVector("LucideCheckCircle") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M22 11.08V12a10 10 0 1 1-5.93-9.14
                moveTo(22f, 11.08f)
                verticalLineTo(12f)
                arcTo(10f, 10f, 0f, true, true, 16.07f, 2.86f)
                // m9 11 3 3L22 4
                moveTo(9f, 11f)
                lineTo(12f, 14f)
                lineTo(22f, 4f)
            }
        }
    }

    val LogOut: ImageVector by lazy {
        lucideVector("LucideLogOut") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4
                moveTo(9f, 21f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 3f, 19f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 5f, 3f)
                horizontalLineTo(9f)
                // m16 17 5-5-5-5
                moveTo(16f, 17f)
                lineTo(21f, 12f)
                lineTo(16f, 7f)
                // M21 12H9
                moveTo(21f, 12f)
                horizontalLineTo(9f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        lucideVector("LucideChevronRight") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m9 18 6-6-6-6
                moveTo(9f, 18f)
                lineTo(15f, 12f)
                lineTo(9f, 6f)
            }
        }
    }

    val ChevronDown: ImageVector by lazy {
        lucideVector("LucideChevronDown") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m6 9 6 6 6-6
                moveTo(6f, 9f)
                lineTo(12f, 15f)
                lineTo(18f, 9f)
            }
        }
    }

    val Check: ImageVector by lazy {
        lucideVector("LucideCheck") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M20 6 9 17l-5-5
                moveTo(20f, 6f)
                lineTo(9f, 17f)
                lineTo(4f, 12f)
            }
        }
    }

    val Sun: ImageVector by lazy {
        lucideVector("LucideSun") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="12" cy="12" r="4"
                moveTo(16f, 12f)
                arcTo(4f, 4f, 0f, true, true, 8f, 12f)
                arcTo(4f, 4f, 0f, true, true, 16f, 12f)
                close()
                // rays
                moveTo(12f, 2f); verticalLineTo(4f)
                moveTo(12f, 20f); verticalLineTo(22f)
                moveTo(4.93f, 4.93f); lineTo(6.34f, 6.34f)
                moveTo(17.66f, 17.66f); lineTo(19.07f, 19.07f)
                moveTo(2f, 12f); horizontalLineTo(4f)
                moveTo(20f, 12f); horizontalLineTo(22f)
                moveTo(6.34f, 17.66f); lineTo(4.93f, 19.07f)
                moveTo(19.07f, 4.93f); lineTo(17.66f, 6.34f)
            }
        }
    }

    val Moon: ImageVector by lazy {
        lucideVector("LucideMoon") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M12 3a6 6 0 0 0 9 9 9 9 0 1 1-9-9Z
                moveTo(12f, 3f)
                arcTo(6f, 6f, 0f, false, false, 21f, 12f)
                arcTo(9f, 9f, 0f, true, true, 12f, 3f)
                close()
            }
        }
    }

    val Monitor: ImageVector by lazy {
        lucideVector("LucideMonitor") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // rect width="20" height="14" x="2" y="3" rx="2"
                moveTo(4f, 3f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 5f)
                verticalLineTo(15f)
                arcTo(2f, 2f, 0f, false, true, 20f, 17f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 15f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 4f, 3f)
                close()
                // line x1="8" x2="16" y1="21" y2="21"
                moveTo(8f, 21f)
                horizontalLineTo(16f)
                // line x1="12" x2="12" y1="17" y2="21"
                moveTo(12f, 17f)
                verticalLineTo(21f)
            }
        }
    }

    val X: ImageVector by lazy {
        lucideVector("LucideX") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M18 6 6 18 M6 6l12 12
                moveTo(18f, 6f)
                lineTo(6f, 18f)
                moveTo(6f, 6f)
                lineTo(18f, 18f)
            }
        }
    }

    val Search: ImageVector by lazy {
        lucideVector("LucideSearch") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="11" cy="11" r="8"
                moveTo(19f, 11f)
                arcTo(8f, 8f, 0f, true, true, 3f, 11f)
                arcTo(8f, 8f, 0f, true, true, 19f, 11f)
                close()
                // m21 21-4.3-4.3
                moveTo(21f, 21f)
                lineTo(16.7f, 16.7f)
            }
        }
    }

    val Folder: ImageVector by lazy {
        lucideVector("LucideFolder") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z
                moveTo(20f, 20f)
                arcTo(2f, 2f, 0f, false, false, 22f, 18f)
                verticalLineTo(8f)
                arcTo(2f, 2f, 0f, false, false, 20f, 6f)
                horizontalLineTo(12.1f)
                arcTo(2f, 2f, 0f, false, true, 10.41f, 5.1f)
                lineTo(9.6f, 3.9f)
                arcTo(2f, 2f, 0f, false, false, 7.93f, 3f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, false, 2f, 5f)
                verticalLineTo(18f)
                arcTo(2f, 2f, 0f, false, false, 4f, 20f)
                close()
            }
        }
    }

    val Server: ImageVector by lazy {
        lucideVector("LucideServer") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // rect width="20" height="8" x="2" y="2" rx="2" ry="2"
                moveTo(4f, 2f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 4f)
                verticalLineTo(8f)
                arcTo(2f, 2f, 0f, false, true, 20f, 10f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 8f)
                verticalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 4f, 2f)
                close()
                // rect width="20" height="8" x="2" y="14" rx="2" ry="2"
                moveTo(4f, 14f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 16f)
                verticalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 20f, 22f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 20f)
                verticalLineTo(16f)
                arcTo(2f, 2f, 0f, false, true, 4f, 14f)
                close()
                // M6 6h.01 M6 18h.01
                moveTo(6f, 6f); horizontalLineTo(6.01f)
                moveTo(6f, 18f); horizontalLineTo(6.01f)
            }
        }
    }

    val MapPin: ImageVector by lazy {
        lucideVector("LucideMapPin") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z
                moveTo(20f, 10f)
                curveTo(20f, 16f, 12f, 22f, 12f, 22f)
                curveTo(12f, 22f, 4f, 16f, 4f, 10f)
                arcTo(8f, 8f, 0f, false, true, 20f, 10f)
                close()
                // circle cx="12" cy="10" r="3"
                moveTo(15f, 10f)
                arcTo(3f, 3f, 0f, true, true, 9f, 10f)
                arcTo(3f, 3f, 0f, true, true, 15f, 10f)
                close()
            }
        }
    }

    val Clock: ImageVector by lazy {
        lucideVector("LucideClock") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="12" cy="12" r="10"
                moveTo(22f, 12f)
                arcTo(10f, 10f, 0f, true, true, 2f, 12f)
                arcTo(10f, 10f, 0f, true, true, 22f, 12f)
                close()
                // polyline points="12 6 12 12 16 14"
                moveTo(12f, 6f)
                verticalLineTo(12f)
                lineTo(16f, 14f)
            }
        }
    }

    val Calendar: ImageVector by lazy {
        lucideVector("LucideCalendar") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M8 2v4 M16 2v4
                moveTo(8f, 2f); verticalLineTo(6f)
                moveTo(16f, 2f); verticalLineTo(6f)
                // rect width="18" height="18" x="3" y="4" rx="2"
                moveTo(5f, 4f)
                horizontalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 21f, 6f)
                verticalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 19f, 22f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 3f, 20f)
                verticalLineTo(6f)
                arcTo(2f, 2f, 0f, false, true, 5f, 4f)
                close()
                // M3 10h18
                moveTo(3f, 10f)
                horizontalLineTo(21f)
            }
        }
    }

    val Eye: ImageVector by lazy {
        lucideVector("LucideEye") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0
                moveTo(2.062f, 12.348f)
                arcTo(1f, 1f, 0f, false, true, 2.062f, 11.652f)
                arcTo(10.75f, 10.75f, 0f, false, true, 21.938f, 11.652f)
                arcTo(1f, 1f, 0f, false, true, 21.938f, 12.348f)
                arcTo(10.75f, 10.75f, 0f, false, true, 2.062f, 12.348f)
                close()
                // circle cx="12" cy="12" r="3"
                moveTo(15f, 12f)
                arcTo(3f, 3f, 0f, true, true, 9f, 12f)
                arcTo(3f, 3f, 0f, true, true, 15f, 12f)
                close()
            }
        }
    }

    val EyeOff: ImageVector by lazy {
        lucideVector("LucideEyeOff") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M10.733 5.076a10.744 10.744 0 0 1 11.205 6.575 1 1 0 0 1 0 .696 10.747 10.747 0 0 1-1.444 2.49
                moveTo(10.733f, 5.076f)
                arcTo(10.744f, 10.744f, 0f, false, true, 21.938f, 11.651f)
                arcTo(1f, 1f, 0f, false, true, 21.938f, 12.347f)
                arcTo(10.747f, 10.747f, 0f, false, true, 20.494f, 14.837f)
                // M14.084 14.158a3 3 0 0 1-4.242-4.242
                moveTo(14.084f, 14.158f)
                arcTo(3f, 3f, 0f, false, true, 9.842f, 9.916f)
                // M17.479 17.499a10.75 10.75 0 0 1-15.417-5.151 1 1 0 0 1 0-.696 10.75 10.75 0 0 1 4.446-5.143
                moveTo(17.479f, 17.499f)
                arcTo(10.75f, 10.75f, 0f, false, true, 2.062f, 12.348f)
                arcTo(1f, 1f, 0f, false, true, 2.062f, 11.652f)
                arcTo(10.75f, 10.75f, 0f, false, true, 6.508f, 6.509f)
                // m2 2 20 20
                moveTo(2f, 2f)
                lineTo(22f, 22f)
            }
        }
    }

    val Maximize: ImageVector by lazy {
        lucideVector("LucideMaximize") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 3f); horizontalLineTo(5f); arcTo(2f, 2f, 0f, false, false, 3f, 5f); verticalLineTo(8f)
                moveTo(21f, 8f); verticalLineTo(5f); arcTo(2f, 2f, 0f, false, false, 19f, 3f); horizontalLineTo(16f)
                moveTo(3f, 16f); verticalLineTo(19f); arcTo(2f, 2f, 0f, false, false, 5f, 21f); horizontalLineTo(8f)
                moveTo(16f, 21f); horizontalLineTo(19f); arcTo(2f, 2f, 0f, false, false, 21f, 19f); verticalLineTo(16f)
            }
        }
    }

    val Minimize: ImageVector by lazy {
        lucideVector("LucideMinimize") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(8f, 3f); verticalLineTo(6f); arcTo(2f, 2f, 0f, false, true, 6f, 8f); horizontalLineTo(3f)
                moveTo(21f, 8f); horizontalLineTo(18f); arcTo(2f, 2f, 0f, false, true, 16f, 6f); verticalLineTo(3f)
                moveTo(3f, 16f); horizontalLineTo(6f); arcTo(2f, 2f, 0f, false, true, 8f, 18f); verticalLineTo(21f)
                moveTo(16f, 21f); verticalLineTo(18f); arcTo(2f, 2f, 0f, false, true, 18f, 16f); horizontalLineTo(21f)
            }
        }
    }

    val Download: ImageVector by lazy {
        lucideVector("LucideDownload") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4
                moveTo(21f, 15f)
                verticalLineTo(19f)
                arcTo(2f, 2f, 0f, false, true, 19f, 21f)
                horizontalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 3f, 19f)
                verticalLineTo(15f)
                // polyline points="7 10 12 15 17 10"
                moveTo(7f, 10f)
                lineTo(12f, 15f)
                lineTo(17f, 10f)
                // line x1="12" x2="12" y1="15" y2="3"
                moveTo(12f, 15f)
                verticalLineTo(3f)
            }
        }
    }

    val Share2: ImageVector by lazy {
        lucideVector("LucideShare2") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="18" cy="5" r="3"
                moveTo(21f, 5f)
                arcTo(3f, 3f, 0f, true, true, 15f, 5f)
                arcTo(3f, 3f, 0f, true, true, 21f, 5f)
                close()
                // circle cx="6" cy="12" r="3"
                moveTo(9f, 12f)
                arcTo(3f, 3f, 0f, true, true, 3f, 12f)
                arcTo(3f, 3f, 0f, true, true, 9f, 12f)
                close()
                // circle cx="18" cy="19" r="3"
                moveTo(21f, 19f)
                arcTo(3f, 3f, 0f, true, true, 15f, 19f)
                arcTo(3f, 3f, 0f, true, true, 21f, 19f)
                close()
                // line x1="8.59" x2="15.42" y1="13.51" y2="17.49"
                moveTo(8.59f, 13.51f)
                lineTo(15.42f, 17.49f)
                // line x1="15.41" x2="8.59" y1="6.51" y2="10.49"
                moveTo(15.41f, 6.51f)
                lineTo(8.59f, 10.49f)
            }
        }
    }

    val Barcode: ImageVector by lazy {
        lucideVector("LucideBarcode") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M3 5v14 M8 5v14 M12 5v14 M17 5v14 M21 5v14
                moveTo(3f, 5f); verticalLineTo(19f)
                moveTo(8f, 5f); verticalLineTo(19f)
                moveTo(12f, 5f); verticalLineTo(19f)
                moveTo(17f, 5f); verticalLineTo(19f)
                moveTo(21f, 5f); verticalLineTo(19f)
            }
        }
    }

    val AlertTriangle: ImageVector by lazy {
        lucideVector("LucideAlertTriangle") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3
                moveTo(21.73f, 18f)
                lineTo(13.73f, 4f)
                arcTo(2f, 2f, 0f, false, false, 10.25f, 4f)
                lineTo(2.25f, 18f)
                arcTo(2f, 2f, 0f, false, false, 4f, 21f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, false, 21.73f, 18f)
                close()
                // M12 9v4 M12 17h.01
                moveTo(12f, 9f); verticalLineTo(13f)
                moveTo(12f, 17f); horizontalLineTo(12.01f)
            }
        }
    }

    val Copy: ImageVector by lazy {
        lucideVector("LucideCopy") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // rect width="14" height="14" x="8" y="8" rx="2" ry="2"
                moveTo(10f, 8f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 10f)
                verticalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 20f, 22f)
                horizontalLineTo(10f)
                arcTo(2f, 2f, 0f, false, true, 8f, 20f)
                verticalLineTo(10f)
                arcTo(2f, 2f, 0f, false, true, 10f, 8f)
                close()
                // M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2
                moveTo(4f, 16f)
                curveTo(2.9f, 16f, 2f, 15.1f, 2f, 14f)
                verticalLineTo(4f)
                curveTo(2f, 2.9f, 2.9f, 2f, 4f, 2f)
                horizontalLineTo(14f)
                curveTo(15.1f, 2f, 16f, 2.9f, 16f, 4f)
            }
        }
    }

    val ArrowLeft: ImageVector by lazy {
        lucideVector("LucideArrowLeft") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m12 19-7-7 7-7 M19 12H5
                moveTo(12f, 19f)
                lineTo(5f, 12f)
                lineTo(12f, 5f)
                moveTo(19f, 12f)
                horizontalLineTo(5f)
            }
        }
    }

    val ChevronLeft: ImageVector by lazy {
        lucideVector("LucideChevronLeft") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m15 18-6-6 6-6
                moveTo(15f, 18f)
                lineTo(9f, 12f)
                lineTo(15f, 6f)
            }
        }
    }

    val FolderOpen: ImageVector by lazy {
        lucideVector("LucideFolderOpen") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m6 14 1.5-2.9A2 2 0 0 1 9.24 10H20a2 2 0 0 1 1.94 2.5l-1.54 6a2 2 0 0 1-1.95 1.5H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h3.9a2 2 0 0 1 1.69.9l.81 1.2a2 2 0 0 0 1.67.9H18a2 2 0 0 1 2 2v2
                moveTo(6f, 14f)
                lineTo(7.5f, 11.1f)
                arcTo(2f, 2f, 0f, false, true, 9.24f, 10f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 21.94f, 12.5f)
                lineTo(20.4f, 18.5f)
                arcTo(2f, 2f, 0f, false, true, 18.45f, 20f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 18f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, true, 4f, 3f)
                horizontalLineTo(7.9f)
                arcTo(2f, 2f, 0f, false, true, 9.59f, 3.9f)
                lineTo(10.4f, 5.1f)
                arcTo(2f, 2f, 0f, false, false, 12.07f, 6f)
                horizontalLineTo(18f)
                arcTo(2f, 2f, 0f, false, true, 20f, 8f)
                verticalLineTo(10f)
            }
        }
    }

    val XCircle: ImageVector by lazy {
        lucideVector("LucideXCircle") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="12" cy="12" r="10"
                moveTo(22f, 12f)
                arcTo(10f, 10f, 0f, true, true, 2f, 12f)
                arcTo(10f, 10f, 0f, true, true, 22f, 12f)
                close()
                // m15 9-6 6 M9 9l6 6
                moveTo(15f, 9f)
                lineTo(9f, 15f)
                moveTo(9f, 9f)
                lineTo(15f, 15f)
            }
        }
    }

    val ImageOff: ImageVector by lazy {
        lucideVector("LucideImageOff") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // line x1="2" x2="22" y1="2" y2="22"
                moveTo(2f, 2f)
                lineTo(22f, 22f)
                // path d="M10.41 10.41a2 2 0 1 1-2.83-2.83"
                moveTo(10.41f, 10.41f)
                arcTo(2f, 2f, 0f, true, true, 7.58f, 7.58f)
                // line x1="13.5" x2="6" y1="13.5" y2="21"
                moveTo(13.5f, 13.5f)
                lineTo(6f, 21f)
                // line x1="18" x2="21" y1="12" y2="15"
                moveTo(18f, 12f)
                lineTo(21f, 15f)
                // path d="M3.59 3.59A1.99 1.99 0 0 0 3 5v14a2 2 0 0 0 2 2h14c.55 0 1.052-.22 1.41-.59"
                moveTo(3.59f, 3.59f)
                arcTo(1.99f, 1.99f, 0f, false, false, 3f, 5f)
                verticalLineTo(19f)
                arcTo(2f, 2f, 0f, false, false, 5f, 21f)
                horizontalLineTo(19f)
                arcTo(2f, 2f, 0f, false, false, 20.41f, 20.41f)
                // path d="M21 15V5a2 2 0 0 0-2-2H9"
                moveTo(21f, 15f)
                verticalLineTo(5f)
                arcTo(2f, 2f, 0f, false, false, 19f, 3f)
                horizontalLineTo(9f)
            }
        }
    }

    val RefreshCw: ImageVector by lazy {
        lucideVector("LucideRefreshCw") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8
                moveTo(3f, 12f)
                arcTo(9f, 9f, 0f, false, true, 12f, 3f)
                arcTo(9.75f, 9.75f, 0f, false, true, 18.74f, 5.74f)
                lineTo(21f, 8f)
                // M21 3v5h-5
                moveTo(21f, 3f)
                verticalLineTo(8f)
                horizontalLineTo(16f)
                // M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, false, true, 12f, 21f)
                arcTo(9.75f, 9.75f, 0f, false, true, 5.26f, 18.26f)
                lineTo(3f, 16f)
                // M8 16H3v5
                moveTo(8f, 16f)
                horizontalLineTo(3f)
                verticalLineTo(21f)
            }
        }
    }

    val RotateCw: ImageVector by lazy {
        lucideVector("LucideRotateCw") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8
                moveTo(21f, 12f)
                arcTo(9f, 9f, 0f, true, true, 12f, 3f)
                curveTo(14.52f, 3f, 16.93f, 4f, 18.74f, 5.74f)
                lineTo(21f, 8f)
                // M21 3v5h-5
                moveTo(21f, 3f)
                verticalLineTo(8f)
                horizontalLineTo(16f)
            }
        }
    }

    val Proportions: ImageVector by lazy {
        lucideVector("LucideProportions") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // rect width="20" height="16" x="2" y="4" rx="2"
                moveTo(4f, 4f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 6f)
                verticalLineTo(18f)
                arcTo(2f, 2f, 0f, false, true, 20f, 20f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 18f)
                verticalLineTo(6f)
                arcTo(2f, 2f, 0f, false, true, 4f, 4f)
                close()
                // M12 9v11
                moveTo(12f, 9f)
                verticalLineTo(20f)
                // M2 9h13a2 2 0 0 1 2 2v9
                moveTo(2f, 9f)
                horizontalLineTo(15f)
                arcTo(2f, 2f, 0f, false, true, 17f, 11f)
                verticalLineTo(20f)
            }
        }
    }

    val StretchHorizontal: ImageVector by lazy {
        lucideVector("LucideStretchHorizontal") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // rect width="20" height="6" x="2" y="4" rx="2"
                moveTo(4f, 4f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 6f)
                verticalLineTo(8f)
                arcTo(2f, 2f, 0f, false, true, 20f, 10f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 8f)
                verticalLineTo(6f)
                arcTo(2f, 2f, 0f, false, true, 4f, 4f)
                close()
                // rect width="20" height="6" x="2" y="14" rx="2"
                moveTo(4f, 14f)
                horizontalLineTo(20f)
                arcTo(2f, 2f, 0f, false, true, 22f, 16f)
                verticalLineTo(18f)
                arcTo(2f, 2f, 0f, false, true, 20f, 20f)
                horizontalLineTo(4f)
                arcTo(2f, 2f, 0f, false, true, 2f, 18f)
                verticalLineTo(16f)
                arcTo(2f, 2f, 0f, false, true, 4f, 14f)
                close()
            }
        }
    }

    val CaretDown: ImageVector by lazy {
        lucideVector("LucideCaretDown") {
            path(
                fill = SolidColor(Color.Black)
            ) {
                // M7 10l5 5 5-5z
                moveTo(7f, 10f)
                lineTo(12f, 15f)
                lineTo(17f, 10f)
                close()
            }
        }
    }

    val History: ImageVector by lazy {
        lucideVector("LucideHistory") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8
                moveTo(3f, 12f)
                arcTo(9f, 9f, 0f, true, false, 12f, 3f)
                arcTo(9.75f, 9.75f, 0f, false, false, 5.26f, 5.74f)
                lineTo(3f, 8f)
                // M3 3v5h5
                moveTo(3f, 3f)
                verticalLineTo(8f)
                horizontalLineTo(8f)
                // M12 7v5l4 2
                moveTo(12f, 7f)
                verticalLineTo(12f)
                lineTo(16f, 14f)
            }
        }
    }

    val Play: ImageVector by lazy {
        lucideVector("LucidePlay") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Tam giác bo tròn 3 đỉnh + cạnh hơi bầu mềm mại (squircle play triangle, cân tâm thị giác)
                moveTo(6.5f, 6.8f)
                quadTo(6.5f, 3.8f, 9.1f, 5.35f)
                quadTo(13.3f, 7.65f, 17.1f, 10.25f)
                quadTo(19.6f, 12.0f, 17.1f, 13.75f)
                quadTo(13.3f, 16.35f, 9.1f, 18.65f)
                quadTo(6.5f, 20.2f, 6.5f, 17.2f)
                quadTo(6.25f, 12.0f, 6.5f, 6.8f)
                close()
            }
        }
    }

    val Pause: ImageVector by lazy {
        lucideVector("LucidePause") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // 2 thanh dọc bo tròn đầu dạng viên thuốc (rx = 1.75)
                moveTo(7.75f, 4.5f)
                horizontalLineTo(8.25f)
                arcTo(1.75f, 1.75f, 0f, false, true, 10f, 6.25f)
                verticalLineTo(17.75f)
                arcTo(1.75f, 1.75f, 0f, false, true, 8.25f, 19.5f)
                horizontalLineTo(7.75f)
                arcTo(1.75f, 1.75f, 0f, false, true, 6f, 17.75f)
                verticalLineTo(6.25f)
                arcTo(1.75f, 1.75f, 0f, false, true, 7.75f, 4.5f)
                close()

                moveTo(15.75f, 4.5f)
                horizontalLineTo(16.25f)
                arcTo(1.75f, 1.75f, 0f, false, true, 18f, 6.25f)
                verticalLineTo(17.75f)
                arcTo(1.75f, 1.75f, 0f, false, true, 16.25f, 19.5f)
                horizontalLineTo(15.75f)
                arcTo(1.75f, 1.75f, 0f, false, true, 14f, 17.75f)
                verticalLineTo(6.25f)
                arcTo(1.75f, 1.75f, 0f, false, true, 15.75f, 4.5f)
                close()
            }
        }
    }

    val Layers: ImageVector by lazy {
        lucideVector("LucideLayers") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // m12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83Z
                moveTo(12f, 2f)
                lineTo(2f, 7f)
                lineTo(12f, 12f)
                lineTo(22f, 7f)
                close()
                // m2 12 10 5 10-5
                moveTo(2f, 12f)
                lineTo(12f, 17f)
                lineTo(22f, 12f)
                // m2 17 10 5 10-5
                moveTo(2f, 17f)
                lineTo(12f, 22f)
                lineTo(22f, 17f)
            }
        }
    }

    val PlusCircle: ImageVector by lazy {
        lucideVector("LucidePlusCircle") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // circle cx="12" cy="12" r="10"
                moveTo(22f, 12f)
                arcTo(10f, 10f, 0f, true, true, 2f, 12f)
                arcTo(10f, 10f, 0f, true, true, 22f, 12f)
                close()
                // M8 12h8 M12 8v8
                moveTo(8f, 12f)
                horizontalLineTo(16f)
                moveTo(12f, 8f)
                verticalLineTo(16f)
            }
        }
    }

    val PhoneCall: ImageVector by lazy {
        lucideVector("LucidePhoneCall") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M15.05 5A5 5 0 0 1 19 8.95
                moveTo(15.05f, 5f)
                arcTo(5f, 5f, 0f, false, true, 19f, 8.95f)
                // M15.05 1A9 9 0 0 1 23 8.94
                moveTo(15.05f, 1f)
                arcTo(9f, 9f, 0f, false, true, 23f, 8.94f)
                // Phone handset: M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z
                moveTo(22f, 16.92f)
                verticalLineToRelative(3f)
                arcToRelative(2f, 2f, 0f, false, true, -2.18f, 2f)
                arcToRelative(19.79f, 19.79f, 0f, false, true, -8.63f, -3.07f)
                arcToRelative(19.5f, 19.5f, 0f, false, true, -6f, -6f)
                arcToRelative(19.79f, 19.79f, 0f, false, true, -3.07f, -8.67f)
                arcTo(2f, 2f, 0f, false, true, 4.11f, 2f)
                horizontalLineToRelative(3f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, 1.72f)
                arcToRelative(12.84f, 12.84f, 0f, false, false, 0.7f, 2.81f)
                arcToRelative(2f, 2f, 0f, false, true, -0.45f, 2.11f)
                lineTo(8.09f, 9.91f)
                arcToRelative(16f, 16f, 0f, false, false, 6f, 6f)
                lineToRelative(1.27f, -1.27f)
                arcToRelative(2f, 2f, 0f, false, true, 2.11f, -0.45f)
                arcToRelative(12.84f, 12.84f, 0f, false, false, 2.81f, 0.7f)
                arcTo(2f, 2f, 0f, false, true, 22f, 16.92f)
                close()
            }
        }
    }

    val BellOff: ImageVector by lazy {
        lucideVector("LucideBellOff") {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // M10.268 21a2 2 0 0 0 3.464 0
                moveTo(10.268f, 21f)
                arcTo(2f, 2f, 0f, false, false, 13.732f, 21f)
                // M17 17H4a1 1 0 0 1-.74-1.673C4.59 13.956 6 12.499 6 8a6 6 0 0 1 .258-1.742
                moveTo(17f, 17f)
                horizontalLineTo(4f)
                arcTo(1f, 1f, 0f, false, true, 3.26f, 15.327f)
                curveTo(4.59f, 13.956f, 6f, 12.499f, 6f, 8f)
                arcTo(6f, 6f, 0f, false, true, 6.258f, 6.258f)
                // M8.668 3.01A6 6 0 0 1 18 8c0 2.687.77 4.653 1.707 6.05
                moveTo(8.668f, 3.01f)
                arcTo(6f, 6f, 0f, false, true, 18f, 8f)
                curveTo(18f, 10.687f, 18.77f, 12.653f, 19.707f, 14.05f)
                // m2 2 20 20
                moveTo(2f, 2f)
                lineTo(22f, 22f)
            }
        }
    }
}

