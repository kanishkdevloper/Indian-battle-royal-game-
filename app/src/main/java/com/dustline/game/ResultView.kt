package com.dustline.game

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

/**
 * Result screen after a match (after a WINNER celebration or after the spectate BACK).
 * The game's own lobby background and the player's 3D character (LobbyAvatarView, standing still) stay behind this view.
 *  - top left: mode + map
 *  - top centre: placement "#rank / players" (win = #1, elimination = number of players still alive + you), WINNER ! / MATCH OVER
 *  - in front of the character: name card with ELIMINATIONS and DAMAGE
 *  - bottom left: survival time + XP / coins, bottom right: BACK (-> loading -> lobby)
 * All sizes are relative to the screen height (u = height / 1080).
 */
class ResultView(ctx: Context) : View(ctx) {
    var onBack: (() -> Unit)? = null

    private var sum: MatchSummary? = null
    private var name = ""
    private var shownAt = 0L
    private var pressed = false
    private val backR = RectF()
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    fun show(s: MatchSummary, playerName: String) {
        sum = s; name = playerName.uppercase(); shownAt = System.nanoTime(); pressed = false
        visibility = VISIBLE; bringToFront(); invalidate()
    }

    private fun ease(x: Float): Float { val t = x.coerceIn(0f, 1f); return 1f - (1f - t) * (1f - t) * (1f - t) }

    override fun onDraw(c: Canvas) {
        val s = sum ?: return
        val w = width.toFloat(); val h = height.toFloat(); val u = h / 1080f
        val age = (System.nanoTime() - shownAt) / 1e9f
        val fade = ease(age / 0.45f)
        fun a(col: Int, k: Float = 1f): Int = (((col ushr 24) * fade * k).toInt().coerceIn(0, 255) shl 24) or (col and 0x00FFFFFF)

        p.style = Paint.Style.FILL; p.shader = null; p.typeface = Ui.FONT_BOLD; p.letterSpacing = 0f; p.textAlign = Paint.Align.LEFT
        // soft shades so the texts read on any background
        p.shader = LinearGradient(0f, 0f, 0f, h * 0.38f, a(0x99000000.toInt()), 0x00000000, Shader.TileMode.CLAMP); c.drawRect(0f, 0f, w, h * 0.38f, p)
        p.shader = LinearGradient(0f, h * 0.8f, 0f, h, 0x00000000, a(0xAA000000.toInt()), Shader.TileMode.CLAMP); c.drawRect(0f, h * 0.8f, w, h, p); p.shader = null

        // ---- top left
        p.color = a(Color.WHITE); p.textSize = 34f * u; c.drawText("BATTLE ROYALE", 40f * u, 62f * u, p)
        p.color = a(0xFFB7C6D6.toInt()); p.textSize = 26f * u; c.drawText("BERMUDA", 40f * u, 96f * u, p)

        // ---- rank  #N / total  (pops in)
        val pop = 1f + 0.35f * (1f - ease((age - 0.1f) / 0.5f))
        val rankTxt = "#${s.place}"; val totTxt = "/${s.total}"
        val rs = 150f * u; val ts = 90f * u
        p.textSize = rs; val rw = p.measureText(rankTxt); p.textSize = ts; val tw = p.measureText(totTxt)
        val x0 = w / 2 - (rw + tw) / 2; val by = 170f * u
        c.save(); c.scale(pop, pop, w / 2, by - rs * 0.35f)
        p.textSize = rs; p.color = a(0x66000000); c.drawText(rankTxt, x0 + 3f * u, by + 4f * u, p)
        p.color = a(if (s.won) Ui.GOLD else 0xFFFFC94D.toInt()); c.drawText(rankTxt, x0, by, p)
        p.textSize = ts; p.color = a(0xFFC9D2DC.toInt(), 0.85f); c.drawText(totTxt, x0 + rw + 4f * u, by, p)
        c.restore()
        // title under the rank
        p.textAlign = Paint.Align.CENTER; p.letterSpacing = 0.12f
        p.textSize = 52f * u; p.color = a(0x88000000.toInt()); c.drawText(if (s.won) "WINNER !" else "MATCH OVER", w / 2 + 2f * u, by + 62f * u, p)
        p.color = a(Color.WHITE); c.drawText(if (s.won) "WINNER !" else "MATCH OVER", w / 2, by + 60f * u, p)
        p.letterSpacing = 0f; p.textAlign = Paint.Align.LEFT

        // ---- name card in front of the character
        val cw = 480f * u; val chh = 196f * u
        val cl = w / 2 - cw / 2; val ct = h * 0.60f
        val card = RectF(cl, ct, cl + cw, ct + chh)
        p.color = a(0xD90A111A.toInt()); c.drawRoundRect(card, 8f * u, 8f * u, p)
        p.color = a(Ui.GOLD); c.drawRect(card.left, card.top + 10f * u, card.left + 6f * u, card.bottom - 10f * u, p)
        p.style = Paint.Style.STROKE; p.strokeWidth = 1.5f * u; p.color = a(0x33FFFFFF); c.drawRoundRect(card, 8f * u, 8f * u, p); p.style = Paint.Style.FILL
        // name (shrinks to fit)
        var ns = 42f * u; p.textSize = ns
        val maxW = cw - 60f * u
        while (p.measureText(name) > maxW && ns > 18f * u) { ns -= 1f * u; p.textSize = ns }
        p.color = a(Color.WHITE); c.drawText(name, card.left + 30f * u, card.top + 56f * u, p)
        p.color = a(0x33FFFFFF); c.drawRect(card.left + 30f * u, card.top + 72f * u, card.right - 30f * u, card.top + 73.5f * u, p)
        // rows
        fun row(y: Float, label: String, value: String) {
            p.textAlign = Paint.Align.LEFT; p.textSize = 29f * u; p.color = a(0xFFE4E9EF.toInt()); c.drawText(label, card.left + 30f * u, y, p)
            p.textAlign = Paint.Align.RIGHT; p.textSize = 33f * u; p.color = a(Ui.GOLD_LIGHT); c.drawText(value, card.right - 30f * u, y, p)
            p.textAlign = Paint.Align.LEFT
        }
        row(card.top + 117f * u, "ELIMINATIONS", s.kills.toString())
        row(card.top + 163f * u, "DAMAGE", s.damage.toString())

        // ---- bottom left: time + rewards
        val mins = s.time.toInt() / 60; val secs = s.time.toInt() % 60
        p.textSize = 26f * u; p.color = a(0xFFB7C6D6.toInt()); c.drawText("SURVIVAL TIME  " + "%d:%02d".format(mins, secs), 40f * u, h - 74f * u, p)
        p.textSize = 30f * u; p.color = a(Ui.GOLD); c.drawText("+${s.xp} XP     +${s.coins} COINS", 40f * u, h - 34f * u, p)

        // ---- bottom right: BACK
        val bw = 300f * u; val bh = 92f * u
        backR.set(w - 40f * u - bw, h - 36f * u - bh, w - 40f * u, h - 36f * u)
        p.color = a(if (pressed) 0xFFC9CED4.toInt() else 0xFFEDEFF2.toInt()); c.drawRoundRect(backR, 6f * u, 6f * u, p)
        p.textAlign = Paint.Align.CENTER; p.textSize = 40f * u; p.color = a(0xFF1D2430.toInt()); c.drawText("BACK", backR.centerX(), backR.centerY() + 14f * u, p)
        p.textAlign = Paint.Align.LEFT

        if (age < 1.2f) postInvalidateOnAnimation()
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { if (backR.contains(e.x, e.y)) { pressed = true; invalidate() } }
            MotionEvent.ACTION_UP -> { val hit = pressed && backR.contains(e.x, e.y); pressed = false; invalidate(); if (hit) onBack?.invoke() }
            MotionEvent.ACTION_CANCEL -> { pressed = false; invalidate() }
        }
        return true
    }
}
