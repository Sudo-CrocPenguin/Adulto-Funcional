/**
 * Pantalla para eliminar definitivamente la cuenta del usuario autenticado.
 *
 * @author Miguel Angel Blandon Montes
 */
import React, { useState } from "react";
import { ActivityIndicator, Alert, Text, TextInput, TouchableOpacity, View, StyleSheet } from "react-native";
import { router } from "expo-router";
import { Colors } from "../../../src/constants/Colors";
import { useProfile } from "../../../src/hooks/useProfile";
import { useAuth } from "../../../src/contexts/AuthContext";

export default function DeleteAccountScreen() {
  const { deleteAccount } = useProfile();
  const { logout } = useAuth();
  const [confirmation, setConfirmation] = useState("");
  const [deleting, setDeleting] = useState(false);

  const handleDelete = async () => {
    if (confirmation.trim().toUpperCase() !== "ELIMINAR") {
      Alert.alert("Error", "Escribe ELIMINAR para confirmar");
      return;
    }

    setDeleting(true);
    try {
      await deleteAccount();
      await logout();
      Alert.alert("Cuenta eliminada", "Tu cuenta y sus datos fueron eliminados.");
      router.replace("/(auth)/login");
    } catch (err: any) {
      Alert.alert("Error", err.response?.data?.message || err.message);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Eliminar cuenta</Text>
      <Text style={styles.subtitle}>Esta accion elimina tu cuenta y todos los datos asociados. No se puede deshacer.</Text>
      <TextInput
        style={styles.input}
        value={confirmation}
        onChangeText={setConfirmation}
        placeholder="Escribe ELIMINAR"
        autoCapitalize="characters"
      />
      <TouchableOpacity style={styles.deleteButton} onPress={handleDelete} disabled={deleting}>
        {deleting ? <ActivityIndicator color="#fff" /> : <Text style={styles.deleteText}>Eliminar definitivamente</Text>}
      </TouchableOpacity>
      <TouchableOpacity style={styles.cancelButton} onPress={() => router.back()} disabled={deleting}>
        <Text style={styles.cancelText}>Cancelar</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, justifyContent: "center", padding: 20, backgroundColor: Colors.background },
  title: { fontSize: 28, fontWeight: "bold", marginBottom: 12, color: Colors.error, textAlign: "center" },
  subtitle: { fontSize: 16, color: Colors.textSecondary, textAlign: "center", marginBottom: 24 },
  input: { borderWidth: 1, borderColor: Colors.border, borderRadius: 12, padding: 12, marginBottom: 16, backgroundColor: "#fff" },
  deleteButton: { backgroundColor: Colors.error, paddingVertical: 14, borderRadius: 30, alignItems: "center" },
  deleteText: { color: "#fff", fontWeight: "bold", fontSize: 16 },
  cancelButton: { alignItems: "center", marginTop: 18 },
  cancelText: { color: Colors.link, fontSize: 16 },
});
