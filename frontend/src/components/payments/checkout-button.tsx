"use client";

import { useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { ApiError } from "@/lib/api/errors";
import { createOrder, createRazorpayOrder, getOrderStatus } from "@/lib/payments/client";

declare global {
  interface Window {
    Razorpay?: new (options: RazorpayOptions) => { open(): void; on(event: string, handler: () => void): void };
  }
}

interface RazorpayOptions {
  key: string;
  amount: number;
  currency: string;
  name: string;
  description?: string;
  order_id: string;
  prefill?: { email?: string };
  theme?: { color?: string };
  handler: () => void;
  modal?: { ondismiss?: () => void };
}

const CHECKOUT_SCRIPT_SRC = "https://checkout.razorpay.com/v1/checkout.js";

function loadRazorpayScript(): Promise<void> {
  return new Promise((resolve, reject) => {
    if (window.Razorpay) return resolve();
    const existing = document.querySelector(`script[src="${CHECKOUT_SCRIPT_SRC}"]`);
    if (existing) {
      existing.addEventListener("load", () => resolve());
      existing.addEventListener("error", () => reject(new Error("Failed to load Razorpay checkout.")));
      return;
    }
    const script = document.createElement("script");
    script.src = CHECKOUT_SCRIPT_SRC;
    script.onload = () => resolve();
    script.onerror = () => reject(new Error("Failed to load Razorpay checkout."));
    document.body.appendChild(script);
  });
}

/** Ref: SRS 10.12 - polls after checkout since the client callback is non-authoritative. */
async function pollUntilSettled(orderId: string, attempts = 15, intervalMs = 2000): Promise<"PAID" | "FAILED" | "PENDING"> {
  for (let i = 0; i < attempts; i++) {
    const order = await getOrderStatus(orderId);
    if (order.status === "PAID" || order.status === "FAILED") return order.status;
    await new Promise((r) => setTimeout(r, intervalMs));
  }
  return "PENDING";
}

type CheckoutState = "idle" | "starting" | "awaiting-payment" | "confirming" | "settled-pending";

/** Ref: SRS 10.3, 10.4, 10.12 - creates an order, launches Razorpay Checkout, then confirms via webhook-backed polling. */
export function CheckoutButton({
  courseId,
  subscriptionPlanId,
  email,
}: {
  courseId: string;
  subscriptionPlanId: string;
  email?: string;
}) {
  const router = useRouter();
  const [state, setState] = useState<CheckoutState>("idle");
  const [orderId, setOrderId] = useState<string | null>(null);
  // A ref, not `state`: the ondismiss/handler callbacks close over the
  // render they were created in, so reading `state` there would always see
  // its value from click-time, never the "confirming" set afterwards.
  const paymentHandledRef = useRef(false);

  async function startCheckout() {
    paymentHandledRef.current = false;
    setState("starting");
    try {
      const order = orderId ? { id: orderId } : await createOrder({ courseId, subscriptionPlanId });
      setOrderId(order.id ?? null);
      if (!order.id) throw new Error("Order was created without an id.");

      const [razorpayOrder] = await Promise.all([createRazorpayOrder(order.id), loadRazorpayScript()]);

      setState("awaiting-payment");
      const razorpay = new window.Razorpay!({
        key: razorpayOrder.razorpayKeyId,
        amount: razorpayOrder.amount,
        currency: razorpayOrder.currency,
        name: "Dhyan Mitra",
        description: "Course subscription",
        order_id: razorpayOrder.razorpayOrderId,
        prefill: { email },
        theme: { color: "#171717" },
        handler: () => {
          paymentHandledRef.current = true;
          setState("confirming");
          pollUntilSettled(order.id!).then((finalStatus) => {
            if (finalStatus === "PAID") {
              toast.success("Payment successful! You're now enrolled.");
              router.refresh();
              setState("idle");
              setOrderId(null);
            } else if (finalStatus === "FAILED") {
              // A fresh order is needed on retry: the backend only allows
              // creating a Razorpay order for a still-PENDING LMS order.
              toast.error("Payment failed. You can try again.");
              setState("idle");
              setOrderId(null);
            } else {
              // Webhook hasn't landed yet - not a failure, just slower than our poll window.
              setState("settled-pending");
            }
          });
        },
        modal: {
          ondismiss: () => {
            if (!paymentHandledRef.current) {
              toast.info("Payment cancelled. You can try again anytime.");
              setState("idle");
            }
          },
        },
      });
      razorpay.open();
    } catch (error) {
      if (error instanceof ApiError && error.status === 503) {
        toast.error("Payments aren't configured yet. Please contact support.");
      } else {
        toast.error(error instanceof ApiError ? error.message : "Something went wrong. Please try again.");
      }
      setState("idle");
    }
  }

  if (state === "confirming") {
    return (
      <Button size="sm" disabled>
        Confirming payment…
      </Button>
    );
  }

  if (state === "settled-pending") {
    return (
      <Button size="sm" variant="outline" onClick={() => router.refresh()}>
        Still processing — refresh
      </Button>
    );
  }

  return (
    <Button size="sm" disabled={state !== "idle"} onClick={startCheckout}>
      {state === "idle" ? "Choose plan" : "Starting…"}
    </Button>
  );
}
