package com.bonfs.tcc.kmpchat.core.mvi

/**
 * Marker interface representing the immutable state of a UI screen.
 */
interface UiState

/**
 * Marker interface representing a user action or intent dispatched from the UI.
 */
interface UiIntent

/**
 * Marker interface representing a one-off side effect (navigation, snackbar, toast).
 */
interface UiEffect
