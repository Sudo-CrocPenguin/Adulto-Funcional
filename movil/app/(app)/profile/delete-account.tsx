/**
 * Pantalla placeholder.
 * TODO: Implementar funcionalidad completa cuando se conecte al backend.
 *
 * @author Miguel Angel Blandon Montes
 */
import React from "react";
import { View, Text, StyleSheet } from "react-native";

export default function PlaceholderScreen() {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>En construcción</Text>
      <Text style={styles.subtitle}>Esta pantalla se implementará próximamente.</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: "center", alignItems: "center", backgroundColor: "#fff" },
  title: { fontSize: 24, fontWeight: "bold", marginBottom: 10 },
  subtitle: { fontSize: 16, color: "#666", textAlign: "center", paddingHorizontal: 20 },
});
